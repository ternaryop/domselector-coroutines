package com.ternaryop.photoshelf.domselector.util

import android.net.Uri
import com.ternaryop.photoshelf.api.ApiManager
import com.ternaryop.photoshelf.api.Response
import com.ternaryop.photoshelf.api.extractor.ImageGalleryResult
import com.ternaryop.photoshelf.api.extractor.ImageInfo
import com.ternaryop.photoshelf.domselector.DomSelectors
import com.ternaryop.photoshelf.domselector.GalleryExtractor
import com.ternaryop.photoshelf.domselector.ImageExtractor
import com.ternaryop.utils.network.UriUtils
import com.ternaryop.utils.network.resolveShorten
import com.ternaryop.utils.network.saveURL
import java.io.File
import java.io.FileOutputStream
import java.net.URL

suspend fun DomSelectors.readImageGallery(url: String): Response<ImageGalleryResult> {
    return GalleryExtractor(this, ApiManager.parserService())
        .getGallery(URL(url).resolveShorten().toString())
}

suspend fun DomSelectors.retrieveImageUri(imageInfo: ImageInfo, destDirectory: File? = null): Uri? {
    val link = getImageURL(imageInfo) ?: return null
    return if (link.isEmpty()) {
        null
    } else {
        makeUri(UriUtils.resolveRelativeURL(imageInfo.documentUrl, link), destDirectory)
    }
}

private suspend fun DomSelectors.getImageURL(imageInfo: ImageInfo): String? {
    // parse document only if the imageURL is not set (ie isn't cached)
    return imageInfo.imageUrl//?.let { it }
        ?: imageInfo.documentUrl?.let { ImageExtractor(this).getImageURL(it) }
}

private fun makeUri(url: String, destDirectory: File?): Uri {
    return if (destDirectory != null) {
        val file = File(destDirectory, url.hashCode().toString())
        FileOutputStream(file).use { fos ->
            URL(url).saveURL(fos)
            Uri.fromFile(file)
        }
    } else {
        Uri.parse(url)
    }
}
