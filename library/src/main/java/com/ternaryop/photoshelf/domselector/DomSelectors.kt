package com.ternaryop.photoshelf.domselector

import com.squareup.moshi.JsonClass
import java.util.regex.Pattern

val emptySelector = Selector("", Image(), Gallery())

interface DomSelectors {
    val version: Int
    val selectors: List<Selector>
    fun getSelectorFromUrl(url: String): Selector {
        return selectors.firstOrNull { Pattern.compile(it.urlPattern).matcher(url).find() } ?: emptySelector
    }
}

@JsonClass(generateAdapter = true)
internal class MutableDomSelectors(
    override var version: Int,
    override var selectors: List<Selector>
) : DomSelectors {
    fun from(source: DomSelectors) {
        version = source.version
        selectors = source.selectors
    }
}

@JsonClass(generateAdapter = true)
data class Gallery(
    val container: String = "a img",
    val isImageDirectUrl: Boolean = false,
    val regExp: String? = null,
    val regExpImageUrlIndex: Int = 0,
    val regExpThumbUrlIndex: Int = 0,
    val title: String? = null,
    val thumbImageSelAttr: String = "src",
    val multiPage: String? = null
) {
    val hasImage: Boolean
        get() = isImageDirectUrl
}

@JsonClass(generateAdapter = true)
class Image(
    val css: String? = null,
    val regExp: String? = null,
    val pageChain: List<PageChain>? = null,
    val postData: PostData? = null,
    val selAttr: String = "src"
) {
    val hasImage: Boolean
        get() = css !== null || regExp !== null || pageChain !== null
}

@JsonClass(generateAdapter = true)
data class PageChain(val pageSel: String, val selAttr: String)

@JsonClass(generateAdapter = true)
data class PostData(val imgContinue: String)

@JsonClass(generateAdapter = true)
data class Selector(
    val urlPattern: String = "",
    val image: Image = Image(),
    val gallery: Gallery = Gallery()
) {
    val hasImage: Boolean
        get() = gallery.hasImage || image.hasImage
}
