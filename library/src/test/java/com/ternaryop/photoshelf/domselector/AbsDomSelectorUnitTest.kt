package com.ternaryop.photoshelf.domselector

import com.squareup.moshi.Moshi
import com.ternaryop.photoshelf.api.ApiManager
import org.junit.Before
import java.io.FileInputStream
import java.util.*

abstract class AbsDomSelectorUnitTest {
    lateinit var properties: Properties
    lateinit var domSelectors: DomSelectors

    @Before
    fun before() {
        properties = Properties()
        javaClass.classLoader!!.getResourceAsStream("config.properties")
            .use { properties.load(it) }

        val jsonBuilder = Moshi
            .Builder()
            .build()
        val fis = FileInputStream(properties.getProperty("domSelectorJsonPath"))
        domSelectors = fis.use { jsonBuilder.domSelectors(it) }
        ApiManager
            .setup(properties.getProperty("apikey"), properties.getProperty("apiPrefix"), null)
    }
}