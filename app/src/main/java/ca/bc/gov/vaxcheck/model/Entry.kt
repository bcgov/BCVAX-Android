package ca.bc.gov.vaxcheck.model

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
