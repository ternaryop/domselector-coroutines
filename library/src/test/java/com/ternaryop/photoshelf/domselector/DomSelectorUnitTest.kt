package com.ternaryop.photoshelf.domselector

import com.ternaryop.photoshelf.domselector.util.readImageGallery
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class DomSelectorUnitTest: AbsDomSelectorUnitTest() {
    @Test
    fun configVersionTest() {
        Assert.assertTrue(domSelectors.version == 6)
    }

    @Test
    fun imageGalleryTest() {
        runBlocking {
            val url = properties.getProperty("htmlPageUrl_01")
            val response = domSelectors.readImageGallery(url).response
            Assert.assertTrue(response.gallery.imageInfoList.size == 10)
        }
    }
}