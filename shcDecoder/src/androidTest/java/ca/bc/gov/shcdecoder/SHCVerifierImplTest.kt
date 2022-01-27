package ca.bc.gov.shcdecoder

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ca.bc.gov.shcdecoder.model.ImmunizationRecord
import ca.bc.gov.shcdecoder.model.ImmunizationStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SHCVerifierImplTest {

    private lateinit var sut: SHCVerifierImpl

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val testRecord = ImmunizationRecord(
        name = DEFAULT_NAME,
        birthDate = DEFAULT_BIRTHDATE,
        status = ImmunizationStatus.INVALID_QR_CODE
    )

    private val config = SHCConfig(
        "https://phsasmarthealthcard-dev.azurewebsites.net/v1/trusted/.well-known/issuers.json",
        "https://ds9mwekyyprcy.cloudfront.net/rules.json",
        emptyList(),
        emptyList(),
        120000L
    )

    @Before
    fun setUp() {
        sut = SHCVerifierImpl(context, config)
    }

    @Test
    fun givenValidSignatureCalled_whenSignatureIsValid_thenReturnsTrue(): Unit = runBlocking {
        val result = sut.hasValidSignature(VALID_FULL_IMMUNIZED_SHC_URI)
        Assert.assertEquals(result, true)
    }

    @Test(expected = SHCDecoderException::class)
    fun givenHasValidateSignatureCalled_whenSignatureIsForged_thenThrowsSHCDecoderException(): Unit = runBlocking {
        sut.hasValidSignature(FORGED_SHC_URI)
    }

    @Test
    fun givenGetImmunizationRecord_whenUserFullyVaccinated_thenShowsFullyImmunized(): Unit = runBlocking {
        val result = sut.getImmunizationRecord(VALID_FULL_IMMUNIZED_SHC_URI)
        Assert.assertEquals(
            result,
            testRecord.copy(
                status = ImmunizationStatus.FULLY_IMMUNIZED
            )
        )
    }

    @Test
    fun givenGetImmunizationRecord_whenUserPartiallyVaccinated_thenShowsPartiallyImmunized(): Unit = runBlocking {
        val result = sut.getImmunizationRecord(VALID_PARTIALLY_IMMUNIZED_SHC_URI)
        Assert.assertEquals(
            result,
            testRecord.copy(
                status = ImmunizationStatus.PARTIALLY_IMMUNIZED
            )
        )
    }

    @Test
    fun givenGetImmunizationRecord_whenUserNotVaccinated_thenShowsInvalidQrCode(): Unit = runBlocking {
        val result = sut.getImmunizationRecord(VALID_NO_VACCINES_SHC_URI)
        Assert.assertEquals(result, testRecord)
    }

    @Test
    fun givenGetImmunizationRecord_whenUserHasExempt_thenShowsFullyImmunized(): Unit = runBlocking {
        val result = sut.getImmunizationRecord(VALID_EXEMPT_SHC_URI)
        Assert.assertEquals(
            result,
            testRecord.copy(
                status = ImmunizationStatus.FULLY_IMMUNIZED
            )
        )
    }
}
