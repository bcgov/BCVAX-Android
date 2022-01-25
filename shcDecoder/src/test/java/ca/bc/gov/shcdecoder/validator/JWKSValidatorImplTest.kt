package ca.bc.gov.shcdecoder.validator

import ca.bc.gov.shcdecoder.TEST_JWK_SIGNATURE
import ca.bc.gov.shcdecoder.TEST_UNSIGNED_PAYLOAD
import ca.bc.gov.shcdecoder.defaultKey
import ca.bc.gov.shcdecoder.utils.derivePublicKey
import ca.bc.gov.shcdecoder.validator.impl.JWKSValidatorImpl
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class JWKSValidatorImplTest : TestCase() {

    private var sut: JWKSValidator = JWKSValidatorImpl()

    @Test
    fun `given on validate when given valid data then returns true`(): Unit = runBlocking {
        val result = sut.validate(derivePublicKey(defaultKey), TEST_UNSIGNED_PAYLOAD, TEST_JWK_SIGNATURE)
        Assert.assertEquals(true, result)
    }

    @Test
    fun `given on validate when invalid payload then returns false`(): Unit = runBlocking {
        val result = sut.validate(derivePublicKey(defaultKey), "", TEST_JWK_SIGNATURE)
        Assert.assertEquals(false, result)
    }

    @Test(expected = Exception::class)
    fun `given on validate when invalid signature then throws exception`(): Unit = runBlocking {
        val result = sut.validate(derivePublicKey(defaultKey), TEST_UNSIGNED_PAYLOAD, "")
        Assert.assertEquals(false, result)
    }
}
