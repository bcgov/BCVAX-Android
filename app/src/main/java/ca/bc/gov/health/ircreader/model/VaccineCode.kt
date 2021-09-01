package ca.bc.gov.health.ircreader.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * [VaccineCode]
 *
 * @author Pinakin Kansara
 */
@Parcelize
data class VaccineCode(
    val coding: List<Coding>
) : Parcelable
