package ca.bc.gov.shcdecoder

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileUtilsTest {

    private lateinit var fileUtils: FileUtils

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        fileUtils = FileUtils(context)
    }

    @Test
    fun downloadFileAndValidate()  = runBlocking{
        val url =
            "https://phsasmarthealthcard-dev.azurewebsites.net/v1/trusted/.well-known/issuers.json"

        fileUtils.downloadFile(url)

        val file = fileUtils.getFile(url)

        assertEquals(file.exists(),true)
    }
}