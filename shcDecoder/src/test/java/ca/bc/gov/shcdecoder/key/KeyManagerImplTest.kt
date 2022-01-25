package ca.bc.gov.shcdecoder.key

import ca.bc.gov.shcdecoder.TEST_ISS
import ca.bc.gov.shcdecoder.TEST_ISS_WITH_SUFFIX
import ca.bc.gov.shcdecoder.TEST_KID
import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.config
import ca.bc.gov.shcdecoder.defaultKey
import ca.bc.gov.shcdecoder.key.impl.KeyManagerImpl
import ca.bc.gov.shcdecoder.model.Issuer
import ca.bc.gov.shcdecoder.utils.derivePublicKey
import ca.bc.gov.shcdecoder.utils.safeCapture
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.internal.verification.Times
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class KeyManagerImplTest {

    private lateinit var sut: KeyManager

    @Mock
    private lateinit var fileManager: FileManager

    @Captor
    private lateinit var keyArgumentCaptor: ArgumentCaptor<String>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        sut = KeyManagerImpl(
            config,
            fileManager
        )
    }

    @Test
    fun `given get public key when data is correct then returns expected key`(): Unit = runBlocking {
        prepareFileManager()
        val resultKey = sut.getPublicKey(TEST_ISS, TEST_KID)
        val expectedKey = derivePublicKey(defaultKey)
        verify(fileManager).getKeys(keyArgumentCaptor.safeCapture())
        Assert.assertEquals(keyArgumentCaptor.value, TEST_ISS_WITH_SUFFIX)
        Assert.assertEquals(resultKey, expectedKey)
    }

    @Test
    fun `given get public key when data is correct and issuers have suffix then returns expected key`(): Unit = runBlocking {
        prepareFileManager(issuer = TEST_ISS_WITH_SUFFIX)
        val resultKey = sut.getPublicKey(TEST_ISS_WITH_SUFFIX, TEST_KID)
        val expectedKey = derivePublicKey(defaultKey)
        verify(fileManager).getKeys(keyArgumentCaptor.safeCapture())
        Assert.assertEquals(keyArgumentCaptor.value, TEST_ISS_WITH_SUFFIX)
        Assert.assertEquals(resultKey, expectedKey)
    }

    @Test
    fun `given get public key when data is invalid then returns default key`(): Unit = runBlocking {
        prepareFileManager(issuer = "")

        val resultKey = sut.getPublicKey(TEST_ISS, TEST_KID)
        val expectedKey = derivePublicKey(defaultKey)

        verify(fileManager, Times(0)).getKeys(anyString())
        Assert.assertEquals(resultKey, expectedKey)
    }

    @Test
    fun `given get public key when exception is threw then returns default key`(): Unit = runBlocking {
        val resultKey = sut.getPublicKey(TEST_ISS, TEST_KID)
        val expectedKey = derivePublicKey(defaultKey)

        verify(fileManager, Times(0)).getKeys(anyString())
        Assert.assertEquals(resultKey, expectedKey)
    }

    private fun prepareFileManager(issuer: String = TEST_ISS): Unit = runBlocking {
        doReturn(
            listOf(
                Issuer(
                    iss = issuer,
                    name = "Dev Freshworks"
                )
            )
        ).`when`(fileManager).getIssuers(anyString())
        doReturn(listOf(defaultKey)).`when`(fileManager).getKeys(anyString())
    }
}
