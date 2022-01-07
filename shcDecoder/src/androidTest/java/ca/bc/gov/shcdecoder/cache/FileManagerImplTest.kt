package ca.bc.gov.shcdecoder.cache

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ca.bc.gov.shcdecoder.cache.impl.FileManagerImpl
import org.junit.Assert.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileNotFoundException

@RunWith(AndroidJUnit4::class)
class FileManagerImplTest {

    private lateinit var sut: FileManager

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        sut = FileManagerImpl(context)
    }

    @Test
    fun onGetRule_fileDownloaded_returnsRules(): Unit = runBlocking {
        sut.downloadFile(TEST_RULES_URL)
        val rules = sut.getRule(TEST_RULES_URL)
        assertTrue(rules.isNotEmpty())
    }

    @Test
    fun onGetKeys_fileDownloaded_returnsKeys(): Unit = runBlocking {
        sut.downloadFile(TEST_KEYS_URL)
        val keys = sut.getKeys(TEST_KEYS_URL)
        assertTrue(keys.isNotEmpty())
    }

    @Test
    fun onGetIssuers_fileDownloaded_returnsIssuers(): Unit = runBlocking {
        sut.downloadFile(TEST_ISSUERS_URL)
        val issuers = sut.getIssuers(TEST_ISSUERS_URL)
        assertTrue(issuers.isNotEmpty())
    }

    @Test(expected = FileNotFoundException::class)
    fun onDownloadFile_onError_throwsException(): Unit = runBlocking {
        sut.downloadFile("non_existent_filename")
        sut.getIssuers("non_existent_filename")
    }

    companion object {
        private const val TEST_RULES_URL = "https://ds9mwekyyprcy.cloudfront.net/rules.json"
        private const val TEST_KEYS_URL = "https://bcvaxcardgen.freshworks.club/.well-known/jwks.json"
        private const val TEST_ISSUERS_URL = "https://phsasmarthealthcard-dev.azurewebsites.net/v1/trusted/.well-known/issuers.json"
    }

}