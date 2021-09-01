package ca.bc.gov.health.ircreader.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * [SHCHeader] holds data retrieved from SMART HEALTH CARD.
 *
 * @author Pinakin Kansara
 */
@Parcelize
data class SHCHeader(
    val zip: String,
    val alg: String,
    val kid: String
) : Parcelable
