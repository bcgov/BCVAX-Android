package ca.bc.gov.health.ircreader

import ca.bc.gov.health.ircreader.model.*
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
        "shc:/5676290952432060346029243740446031222959532654603460292540772804336028702864716745222809286133314564376531415906402203064504590856435503414245413640370636654171372412363803043756220467374075323239254334433260573601064529315312707424283950386922127665686369603555095837280503333766266272593611037153444411222636632120060327507331720558534523626325330442062707544253282104205936207112612433666076283230353358592811122259061043206957593631735937620359663140280669287426080603254457096673644465635631503363030536743842585728210726593553652700453906317361703422214022506963232661415312244132090030673772406306417422442309532523260843360611577127546928636552121169576620063352562900435640401150697056535256373337092326100624640536573958734530360828677743376162106620270058612436567157105933366256121054632237092745112357302010710874092574383324263042292175117152437528450077415929451050342909657154085326443072254040506267244563433370636840045228724445097745120440691073085026502528446607595938433177746043122062672536423104054322332904767209391143202824540667505523331227684134101173723362606830093603770321453366295553642256393365322974683827450509332676711011532929263954733767605435563027332404226374765326666867067759710761567524763470502839664121600439650073317026600409665411007239226630233641373841682510003445707153664075571242302312435045443305115544326943676167007403017209384432430761580855684522040435343359726135344563302174440529083830570472064042773660212076743522544066542527533173534341310605386708367039501152104268615675743771425436"


    @Test
    fun shcPayloadDecodedPass() {

        var shcData: SHCData? = null
        val decoder = SHCDecoder()
        decoder.decode(
            shcUri,
            onSuccess =
            {
                shcData = it
            }, onError = {

            }
        )

        val expectedSHCData = getSampleShcData()

        assertNotNull(shcData)

        //assert SHC Header
        assertNotNull(shcData?.header)

        assertEquals(expectedSHCData.header.alg, shcData?.header?.alg)
        assertEquals(expectedSHCData.header.zip, shcData?.header?.zip)
        assertEquals(expectedSHCData.header.kid, shcData?.header?.kid)

        //assert SHC signature
        assertNotNull(shcData?.signature)

        assertEquals(expectedSHCData.signature, shcData?.signature)

        //assert SHC payload
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
        return "u6SYMX4jg5dqZC11PONhujPOZlKBwY2J5SKf1u3UWzQiBAywPCcUocFHbLvbXVL32Sp5QsT_8a7WqjexwRtWcQ"
    }
}