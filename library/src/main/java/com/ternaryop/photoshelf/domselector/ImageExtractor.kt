package com.ternaryop.photoshelf.domselector

import android.net.Uri
import com.ternaryop.photoshelf.domselector.util.html.HtmlDocumentSupport
import kotlinx.coroutines.coroutineScope
import org.jsoup.Jsoup
import java.text.ParseException
import java.util.regex.Pattern

class ImageExtractor(private val domSelectors: DomSelectors) {
    suspend fun getImageURL(documentUrl: String): String {
        val selector = domSelectors.getSelectorFromUrl(documentUrl)
        val image = selector.image
        var url = ""

        if (image.css != null) {
            url = urlFromCSS3Selector(image, documentUrl)
        }
        if (url.isBlank() && image.regExp != null) {
            url = urlFromRegExp(selector, documentUrl)
        }
        if (url.isBlank() && image.pageChain != null) {
            url = urlFromChain(image, documentUrl)
        }
        if (url.isBlank()) {
            url = documentUrl
        }
        val uri = Uri.parse(documentUrl)
        val uriScheme = checkNotNull(uri.scheme) { "Invalid image url: $documentUrl" }
        val baseuri = uriScheme + "://" + uri.host
        return HtmlDocumentSupport.encodeUrlRfc3986(HtmlDocumentSupport.absUrl(baseuri, url))
    }

    private suspend fun urlFromCSS3Selector(selector: Image, documentUrl: String): String {
        selector.css ?: return ""
        return getDocumentFromUrl(documentUrl).select(selector.css).attr(selector.selAttr) ?: ""
    }

    private suspend fun urlFromChain(selector: Image, documentUrl: String): String {
        selector.pageChain?.also { return getImageUrlFromPageSel(it, documentUrl) }
        return ""
    }

    private fun urlFromRegExp(selector: Selector, documentUrl: String): String {
        val html = HtmlDocumentSupport
            .download(documentUrl, selector.toDownloadOptions())
            .replace("""([\n\r\t])""".toRegex(), "")
        selector.image.regExp?.also { regExp ->
            val m = Pattern.compile(regExp).matcher(html)
            if (m.find()) {
                val url = m.group(1)
                if (url != null) {
                    return url
                }
            }
        }
        throw ParseException("Unable to find image url for $documentUrl", 0)
    }

    /**
     * Iterate all PageSelector to find the destination image url.
     * Every PageSelector moves to an intermediate document page
     * @param pageChainList the list to traverse
     * @param url the starting document url
     * @return the imageUrl
     */
    private suspend fun getImageUrlFromPageSel(pageChainList: List<PageChain>, url: String): String {
        var imageUrl = url

        for (pc in pageChainList) {
            imageUrl = getDocumentFromUrl(imageUrl).select(pc.pageSel).attr(pc.selAttr)
        }
        return imageUrl
    }

    private suspend fun getDocumentFromUrl(url: String) = coroutineScope {
        val domSelector = domSelectors.getSelectorFromUrl(url)
        Jsoup.parse(HtmlDocumentSupport.download(url, domSelector.toDownloadOptions()))
    }
}
