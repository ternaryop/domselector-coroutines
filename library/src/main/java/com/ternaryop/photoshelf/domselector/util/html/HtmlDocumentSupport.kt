package com.ternaryop.photoshelf.domselector.util.html

import android.net.Uri
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val BUFFER_SIZE = 200 * 1024

data class SrcSetItem(val width: Int, val url: String)
data class DownloadOptions(
    val postData: String? = null,
    val userAgent: String? = HtmlDocumentSupport.DESKTOP_USER_AGENT,
    val cookie: String? = null
)

/**
 * Created by dave on 07/05/15.
 * Helper class to read Http documents from urls
 */
@Suppress("MemberVisibilityCanBePrivate")
object HtmlDocumentSupport {
    const val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:57.0) Gecko/20100101 Firefox/57.0"

    /**
     * Open connection using the passed options if any
     * @param urlString the url to open
     * @param downloadOptions the options
     * @return the connection
     * @throws IOException when open fails
     */
    fun openConnection(
        urlString: String,
        downloadOptions: DownloadOptions? = null
    ): HttpURLConnection {
        var url = URL(urlString)
        var conn: HttpURLConnection
        var location: String
        var continueFollow = true

        val userAgent = downloadOptions?.userAgent ?: DESKTOP_USER_AGENT
        val postData = downloadOptions?.postData

        do {
            conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", userAgent)
            conn.instanceFollowRedirects = false

            downloadOptions?.cookie?.also { cookie ->
                conn.setRequestProperty("Cookie", cookie)
            }

            if (postData != null) {
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-length", postData.length.toString())

                conn.doOutput = true
                conn.doInput = true
                val output = DataOutputStream(conn.outputStream)
                output.writeBytes(postData)
                output.close()
            }

            when (conn.responseCode) {
                HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_MOVED_TEMP -> {
                    location = conn.getHeaderField("Location")
                    // Deal with relative URLs
                    url = URL(url, location)
                }
                else -> continueFollow = false
            }
        } while (continueFollow)
        return conn
    }

    fun absUrl(baseuri: String, rel: String): String {
        if (rel.indexOf("http://") == 0 || rel.indexOf("https://") == 0) {
            return rel
        }
        return "$baseuri/$rel"
    }

    fun download(
        url: String,
        downloadOptions: DownloadOptions? = null
    ): String {
        var connection: HttpURLConnection? = null

        try {
            connection = openConnection(url, downloadOptions)
            ByteArrayOutputStream().use { os ->
                val bos = BufferedOutputStream(os)
                connection.inputStream?.use { it.copyTo(bos, BUFFER_SIZE) }
                bos.flush()
                return String(os.toByteArray(), Charsets.UTF_8)
            }
        } finally {
            try {
                connection?.disconnect()
            } catch (ignored: Exception) {
            }
        }
    }

    /**
     * Return the map (width, url) for HTML srcset attribute's content sorted by width
     */
    fun parseSrcSet(srcSet: String): List<SrcSetItem>? {
        if (srcSet.isBlank()) {
            return null
        }
        val list = mutableListOf<SrcSetItem>()
        for (src in srcSet.split(",")) {
            // split url and width
            val pair = src.trim().split(" ")
            if (pair.size == 2) {
                list.add(SrcSetItem(pair[1].trim().replace("w", "").toInt(), pair[0].trim()))
            }
        }
        list.sortWith { lhr, rhs -> lhr.width - rhs.width }

        return list
    }

    @Suppress("ReturnCount")
    fun encodeUrlRfc3986(url: String): String {
        val uri = Uri.parse(url)
        val scheme = uri.scheme ?: return url
        val path = uri.path ?: return url
        val port = if (uri.port == -1) "" else ":${uri.port}"
        val query = if (uri.query == null) "" else "?${uri.query}"
        return "$scheme://${uri.host}$port${encodeUrlPath(path)}$query"
    }

    fun encodeUrlPath(path: String): String {
        val encodePath = StringBuilder()

        for (p in path.split("/")) {
            if (p.isNotEmpty()) {
                encodePath.append("/")
            }
            encodePath.append(URLEncoder.encode(p, "UTF-8"))
        }
        return encodePath.toString()
    }
}
