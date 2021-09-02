package ca.bc.gov.vaxcheck.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * [Coding]
 *
 * @author Pinakin Kansara
 */
@Parcelize
data class Coding(
    val system: String,
    val code: String
) : Parcelable
