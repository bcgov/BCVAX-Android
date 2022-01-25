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
    fun `given get unsigned jwks payload when shcuri is valid then returns correct payload`(): Unit = runBlocking {
        val result = sut.getUnSignedJWKSPayload(VALID_FULLY_IMMUNIZED_SHC_URI)
        Assert.assertEquals(result, TEST_UNSIGNED_PAYLOAD)
    }

    @Test(expected = SHCDecoderException::class)
    fun `given get unsigned jwks payload when shcuri is invalid then throws exception`(): Unit = runBlocking {
        sut.getUnSignedJWKSPayload("")
    }

    @Test
    fun `given get jwks signature when shcuri is valid then returns correct signature`(): Unit = runBlocking {
        val result = sut.getJWKSignature(VALID_FULLY_IMMUNIZED_SHC_URI)
        Assert.assertEquals(result, TEST_JWK_SIGNATURE)
    }

    @Test(expected = SHCDecoderException::class)
    fun `given get jwks signature when shcuri is invalid then throws exception`(): Unit = runBlocking {
        sut.getJWKSignature("")
    }

    @Test
    fun `given on parse when shc is valid then returns correct signature string` (): Unit = runBlocking {
        val result = sut.parse(VALID_FULLY_IMMUNIZED_SHC_URI)
        Assert.assertEquals(result.signature, TEST_JWK_SIGNATURE)
    }

}