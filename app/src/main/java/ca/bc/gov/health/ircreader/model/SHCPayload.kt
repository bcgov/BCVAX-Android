package ca.bc.gov.health.ircreader.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * [SHCPayload] holds SMART HEALTH CARD DATA.
 *
 * @author Pinakin Kansara
 */
@Parcelize
data class SHCPayload(
    val iss: String,
    val nbf: Double,
    val vc: Vc
) : Parcelable
