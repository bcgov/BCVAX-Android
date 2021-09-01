package ca.bc.gov.health.ircreader.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * [FhirBundle]
 *
 * @author Pinakin Kansara
 */
@Parcelize
data class FhirBundle(
    val resourceType: String,
    val type: String,
    val entry: List<Entry>
) : Parcelable
