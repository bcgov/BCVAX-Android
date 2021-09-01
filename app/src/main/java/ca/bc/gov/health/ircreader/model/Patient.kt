package ca.bc.gov.health.ircreader.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * [Patient]
 *
 * @author Pinakin Kansara
 */
@Parcelize
data class Patient(
    val reference: String
) : Parcelable
