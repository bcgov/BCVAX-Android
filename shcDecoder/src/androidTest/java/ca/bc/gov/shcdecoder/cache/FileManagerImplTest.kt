package ca.bc.gov.shcdecoder.cache

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ca.bc.gov.shcdecoder.TEST_ISSUERS_URL
import ca.bc.gov.shcdecoder.TEST_KEYS_URL
import ca.bc.gov.shcdecoder.TEST_RULES_URL
import ca.bc.gov.shcdecoder.cache.impl.FileManagerImpl
import org.junit.Assert
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
    fun givenGetRule_whenFileDownloaded_thenReturnsRules(): Unit = runBlocking {
        sut.downloadFile(TEST_RULES_URL)
        val rules = sut.getRule(TEST_RULES_URL)
        Assert.assertTrue(rules.isNotEmpty())
    }

    @Test
    fun givenGetKeys_whenFileDownloaded_thenReturnsKeys(): Unit = runBlocking {
        sut.downloadFile(TEST_KEYS_URL)
        val keys = sut.getKeys(TEST_KEYS_URL)
        Assert.assertTrue(keys.isNotEmpty())
    }

    @Test
    fun givenGetIssuers_whenFileDownloaded_thenReturnsIssuers(): Unit = runBlocking {
        sut.downloadFile(TEST_ISSUERS_URL)
        val issuers = sut.getIssuers(TEST_ISSUERS_URL)
        Assert.assertTrue(issuers.isNotEmpty())
    }

    @Test(expected = FileNotFoundException::class)
    fun givenDownloadFile_whenError_thenThrowsException(): Unit = runBlocking {
        sut.downloadFile("non_existent_filename")
        sut.getIssuers("non_existent_filename")
    }

}