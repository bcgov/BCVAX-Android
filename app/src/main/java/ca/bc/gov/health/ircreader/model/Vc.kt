package ca.bc.gov.health.ircreader.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * [Vc]
 *
 * @author Pinakin Kansara
 */
@Parcelize
data class Vc(
    val type: List<String>,
    val credentialSubject: CredentialSubject
) : Parcelable
