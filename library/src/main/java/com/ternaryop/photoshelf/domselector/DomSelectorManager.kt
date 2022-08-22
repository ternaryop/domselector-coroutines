package com.ternaryop.photoshelf.domselector

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.squareup.moshi.Moshi
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

internal fun Moshi.domSelectors(input: InputStream) =
    input.bufferedReader().use {
        checkNotNull(adapter(MutableDomSelectors::class.java).fromJson(it.readText()))
    }

/**
 * Obtain the DOM selector used to extract gallery and images contained inside a given url
 *
 * @author dave
 */
object DomSelectorManager {
    private var domSelectors: MutableDomSelectors? = null
    private const val SELECTORS_FILENAME = "domSelectors.json"

    fun selectors(context: Context): DomSelectors = buildSelectors(context, false)

    private fun buildSelectors(context: Context, reloadConfig: Boolean): DomSelectors {
        synchronized(MutableDomSelectors::class.java) {
            val forceReload = if (domSelectors == null) {
                domSelectors = MutableDomSelectors(-1, emptyList())
                true
            } else {
                reloadConfig
            }

            if (forceReload) {
                try {
                    domSelectors!!.from(openConfig(context))
                } catch (ignored: IOException) {
                }
            }
        }
        return domSelectors!!
    }

    private fun openConfig(context: Context): DomSelectors {
        val jsonBuilder = Moshi
            .Builder()
            .build()
        return try {
            // if an imported file exists and its version is minor than the file in assets we delete it
            val importedSelectors = context.openFileInput(SELECTORS_FILENAME).use { jsonBuilder.domSelectors(it) }
            val assetsSelectors = context.assets.open(SELECTORS_FILENAME).use { jsonBuilder.domSelectors(it) }

            if (importedSelectors.version >= assetsSelectors.version) {
                importedSelectors
            } else {
                context.deleteFile(SELECTORS_FILENAME)
                assetsSelectors
            }
        } catch (ignored: FileNotFoundException) {
            context.assets.open(SELECTORS_FILENAME).use { jsonBuilder.domSelectors(it) }
        }
    }

    private fun copyConfig(context: Context, input: InputStream) {
        input.use { stream -> context.openFileOutput(SELECTORS_FILENAME, 0).use { out -> stream.copyTo(
            out
        ) } }
    }

    fun upgradeConfig(context: Context, uri: Uri) {
        checkNotNull(context.contentResolver.openInputStream(uri)) { "Unable to read configuration" }.also { stream ->
            copyConfig(context, stream)
            DocumentsContract.deleteDocument(context.contentResolver, uri)
            buildSelectors(context, true)
        }
    }
}
