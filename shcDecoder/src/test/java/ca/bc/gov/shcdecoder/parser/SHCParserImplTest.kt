package ca.bc.gov.shcdecoder.parser

import ca.bc.gov.shcdecoder.SHCDecoderException
import ca.bc.gov.shcdecoder.TEST_JWK_SIGNATURE
import ca.bc.gov.shcdecoder.TEST_UNSIGNED_PAYLOAD
import ca.bc.gov.shcdecoder.VALID_FULLY_IMMUNIZED_SHC_URI
import ca.bc.gov.shcdecoder.parser.impl.SHCParserImpl
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SHCParserImplTest : TestCase() {

    private val sut: SHCParser = SHCParserImpl()

    @Test
    fun onGetUnSignedJWKSPayload_givenValidShcUri_returnsCorrectPayload(): Unit = runBlocking {
        val result = sut.getUnSignedJWKSPayload(VALID_FULLY_IMMUNIZED_SHC_URI)
        Assert.assertEquals(result, TEST_UNSIGNED_PAYLOAD)
    }

    @Test(expected = SHCDecoderException::class)
    fun onGetUnSignedJWKSPayload_givenInvalidShcUri_throwsException(): Unit = runBlocking {
        sut.getUnSignedJWKSPayload("")
    }

    @Test
    fun onGetJWKSignature_givenValidShcUri_returnsCorrectSignature(): Unit = runBlocking {
        val result = sut.getJWKSignature(VALID_FULLY_IMMUNIZED_SHC_URI)
        Assert.assertEquals(result, TEST_JWK_SIGNATURE)
    }

    @Test(expected = SHCDecoderException::class)
    fun onGetJWKSignature_givenInvalidShcUri_throwsException(): Unit = runBlocking {
        sut.getJWKSignature("")
    }

    @Test
    fun onParse_givenValidShcUri_returnsCorrectSignatureString (): Unit = runBlocking {
        val result = sut.parse(VALID_FULLY_IMMUNIZED_SHC_URI)
        Assert.assertEquals(result.signature, TEST_JWK_SIGNATURE)
    }

}