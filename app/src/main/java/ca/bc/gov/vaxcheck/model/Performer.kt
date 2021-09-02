package ca.bc.gov.vaxcheck.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * [Performer]
 *
 * @author Pinakin Kansara
 */
@Parcelize
data class Performer(
    val actor: Actor
) : Parcelable
