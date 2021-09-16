package ca.bc.gov.vaxcheck.utils

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import ca.bc.gov.vaxcheck.R

/**
 * Helper function to read file from asset
 * and return String JSON.
 */
fun Context.readJsonFromAsset(fileName: String) =
    this.assets.open(fileName).bufferedReader().use { it.readText() }

fun TextView.setSpannableLink(text: String? = null, onClick: () -> Unit) {
    val spannableString = SpannableString(
        if (text.isNullOrBlank()) {
            this.text
        } else {
            text
        }
    )
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(p0: View) {
            onClick()

        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = resources.getColor(R.color.white, null)
        }
    }

    spannableString.setSpan(
        clickableSpan,
        0,
        spannableString.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
    this.movementMethod = LinkMovementMethod.getInstance()
}

fun Context.toast(message: String)
        = Toast.makeText(this, message, Toast.LENGTH_LONG).show()