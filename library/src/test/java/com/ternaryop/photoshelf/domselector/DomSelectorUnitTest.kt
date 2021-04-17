package com.ternaryop.photoshelf.domselector

import com.squareup.moshi.Moshi
import org.junit.Assert
import org.junit.Test
import java.io.FileInputStream

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class DomSelectorUnitTest {
    @Test
    fun readTest() {
        val jsonBuilder = Moshi
            .Builder()
            .build()
        val fis = FileInputStream("/Volumes/Devel/devel/0dafiprj/git.github/android/photoshelf/app/src/main/assets/domSelectors.json")
        val importedSelectors = fis.use { jsonBuilder.domSelectors(it) }
        Assert.assertTrue(importedSelectors.version == 6)
    }
}