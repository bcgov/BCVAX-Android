package ca.bc.gov.health.ircreader.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * [Entry]
 *
 * @author Pinakin Kansara
 */
@Parcelize
data class Entry(
    val fullUrl: String,
    val resource: Resource
) : Parcelable
