package ca.bc.gov.health.ircreader

import ca.bc.gov.health.ircreader.model.CredentialSubject
import ca.bc.gov.health.ircreader.model.Entry
import ca.bc.gov.health.ircreader.model.FhirBundle
import ca.bc.gov.health.ircreader.model.Name
import ca.bc.gov.health.ircreader.model.Resource
import ca.bc.gov.health.ircreader.model.SHCData
import ca.bc.gov.health.ircreader.model.SHCHeader
import ca.bc.gov.health.ircreader.model.SHCPayload
import ca.bc.gov.health.ircreader.model.Vc
import ca.bc.gov.health.ircreader.utils.SHCDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * [SHCDecoderTest]
 * This class will validate SHC Data decoder.
 *
 * @author Pinakin Kansara
 */
class SHCDecoderTest {

    private val shcUri =
        "shc:/56762909524320603460292437404460312229595326546034602925" +
            "40772804336028702864716745222809286133314564376531415" +
            "90640220306450459085643550341424541364037063665417137" +
            "24123638030437562204673740753232392543344332605736010" +
            "64529315312707424283950386922127665686369603555095837" +
            "28050333376626627259361103715344441122263663212006032" +
            "75073317205585345236263253304420627075442532821042059" +
            "36207112612433666076283230353358592811122259061043206" +
            "95759363173593762035966314028066928742608060325445709" +
            "66736444656356315033630305367438425857282107265935536" +
            "52700453906317361703422214022506963232661415312244132" +
            "09003067377240630641742244230953252326084336061157712" +
            "75469286365521211695766200633525629004356404011506970" +
            "56535256373337092326100624640536573958734530360828677" +
            "74337616210662027005861243656715710593336625612105463" +
            "22370927451123573020107108740925743833242630422921751" +
            "17152437528450077415929451050342909657154085326443072" +
            "25404050626724456343337063684004522872444509774512044" +
            "06910730850265025284466075959384331777460431220626725" +
            "36423104054322332904767209391143202824540667505523331" +
            "22768413410117372336260683009360377032145336629555364" +
            "22563933653229746838274505093326767110115329292639547" +
            "33767605435563027332404226374765326666867067759710761" +
            "56752476347050283966412160043965007331702660040966541" +
            "10072392266302336413738416825100034457071536640755712" +
            "42302312435045443305115544326943676167007403017209384" +
            "43243076158085568452204043534335972613534456330217444" +
            "05290838305704720640427736602120767435225440665425275" +
            "33173534341310605386708367039501152104268615675743771" +
            "425436"

    @Test
    fun shcPayloadDecodedPass() {

        var shcData: SHCData? = null
        val decoder = SHCDecoder()
        decoder.decode(
            shcUri,
            onSuccess =
            {
                shcData = it
            },
            onError = {
            }
        )

        val expectedSHCData = getSampleShcData()

        assertNotNull(shcData)

        // assert SHC Header
        assertNotNull(shcData?.header)

        assertEquals(expectedSHCData.header.alg, shcData?.header?.alg)
        assertEquals(expectedSHCData.header.zip, shcData?.header?.zip)
        assertEquals(expectedSHCData.header.kid, shcData?.header?.kid)

        // assert SHC signature
        assertNotNull(shcData?.signature)

        assertEquals(expectedSHCData.signature, shcData?.signature)

        // assert SHC payload
        assertNotNull(shcData?.payload)

        assertEquals(expectedSHCData.payload.iss, shcData?.payload?.iss)
        assertEquals(expectedSHCData.payload.nbf, shcData?.payload?.nbf)
        assertEquals(expectedSHCData.payload.vc.type.size, shcData?.payload?.vc?.type?.size)
        assertEquals(
            expectedSHCData.payload.vc.credentialSubject.fhirVersion,
            shcData?.payload?.vc?.credentialSubject?.fhirVersion
        )
        assertEquals(
            expectedSHCData.payload.vc.credentialSubject.fhirBundle.entry.size,
            shcData?.payload?.vc?.credentialSubject?.fhirBundle?.entry?.size
        )
        assertEquals(
            expectedSHCData.payload.vc.credentialSubject.fhirBundle.resourceType,
            shcData?.payload?.vc?.credentialSubject?.fhirBundle?.resourceType
        )
        assertEquals(
            expectedSHCData.payload.vc.credentialSubject.fhirBundle.type,
            shcData?.payload?.vc?.credentialSubject?.fhirBundle?.type
        )
    }

    private fun getSampleShcData(): SHCData {
        return SHCData(
            getSampleSHCHeader(),
            getSampleSHCPayload(),
            getSampleSHCSignature()
        )
    }

    private fun getSampleSHCHeader(): SHCHeader {
        return SHCHeader(
            zip = "DEF",
            alg = "ES256",
            kid = "3Kfdg-XwP-7gXyywtUfUADwBumDOPKMQx-iELL11W9s"
        )
    }

    private fun getSampleSHCPayload(): SHCPayload {
        return SHCPayload(
            iss = "https://spec.smarthealth.cards/examples/issuer",
            nbf = 1628985325.599,
            vc = getSampleVC()
        )
    }

    private fun getSampleVC(): Vc {
        return Vc(
            type = listOf(
                "https://smarthealth.cards#health-card",
                "https://smarthealth.cards#immunization",
                "https://smarthealth.cards#covid19"
            ),
            credentialSubject = CredentialSubject(
                fhirVersion = "4.0.1",
                fhirBundle = getSampleFhirBundle()
            )
        )
    }

    private fun getSampleFhirBundle(): FhirBundle {
        return FhirBundle(
            resourceType = "Bundle",
            type = "collection",
            entry = getSampleEntries()
        )
    }

    private fun getSampleEntries() = listOf(
        Entry(
            fullUrl = "resource:0",
            resource = Resource(
                resourceType = "Patient",
                name = listOf(
                    Name(
                        family = "Anyperson",
                        given = listOf(
                            "John",
                            "B."
                        )
                    )
                ),
                birthDate = "1951-01-20"

            )
        ),
        Entry(
            fullUrl = "resource:0",
            resource = Resource(
                resourceType = "Patient",
                name = listOf(
                    Name(
                        family = "Anyperson",
                        given = listOf(
                            "John",
                            "B."
                        )
                    )
                ),
                birthDate = "1951-01-20"

            )
        ),
        Entry(
            fullUrl = "resource:0",
            resource = Resource(
                resourceType = "Patient",
                name = listOf(
                    Name(
                        family = "Anyperson",
                        given = listOf(
                            "John",
                            "B."
                        )
                    )
                ),
                birthDate = "1951-01-20"

            )
        )
    )

    private fun getSampleSHCSignature(): String {
        return "u6SYMX4jg5dqZC11PONhujPOZlKBwY2J5SKf1u3" +
            "UWzQiBAywPCcUocFHbLvbXVL32Sp5QsT_8a7WqjexwRtWcQ"
    }
}
