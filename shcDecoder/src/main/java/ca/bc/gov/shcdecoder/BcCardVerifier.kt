package ca.bc.gov.shcdecoder

import android.content.Context
import ca.bc.gov.shcdecoder.model.ImmunizationRecord
import ca.bc.gov.shcdecoder.repository.KeysRepository

class BcCardVerifier(
    context: Context
) {

    private lateinit var shcDecoder: SHCDecoder

    private val keysRepository: KeysRepository = KeysRepository(context)

    fun verify(shcUri: String): ImmunizationRecord {

        shcDecoder = SHCDecoder(shcUri)

        val iss = shcDecoder.getIss()

        val jwks = keysRepository.getKeyForIssuer(iss)
            ?: throw SHCDecoderException(
                SHCDecoderException.ID_SIGNATURE_KEY_NOT_FOUND,
                SHCDecoderException.MESSAGE_SIGNATURE_KEY_NOT_FOUND
            )

        if (!shcDecoder.validateSignature(jwks, shcUri)) {
            throw SHCDecoderException(
                SHCDecoderException.ID_INVALID_SIGNATURE_KEY,
                SHCDecoderException.MESSAGE_INVALID_SIGNATURE_KEY
            )
        }

        val ruleSet = keysRepository.getRuleSetForIssuer(iss) ?: throw SHCDecoderException(
            SHCDecoderException.ID_SIGNATURE_KEY_NOT_FOUND,
            SHCDecoderException.MESSAGE_SIGNATURE_KEY_NOT_FOUND
        )

        return shcDecoder.determineImmunizationStatus(shcUri, ruleSet)
    }
}