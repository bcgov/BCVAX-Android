package ca.bc.gov.shcdecoder.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

fun Context.hasNetwork(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
    return (activeNetwork != null && activeNetwork.isConnected)
}

/**
 * Helper function to read file from asset
 * and return String JSON.
 */
fun Context.readJsonFromAsset(fileName: String) =
    this.assets.open(fileName).bufferedReader().use { it.readText() }

/**
 *  Converts Epoch Timestamp To Date
 *  Epoch is in seconds instead of millis
 */
fun String.epochToDate(): Date? {
    return try {
        Date(this.toLong().times(1000))
    } catch (e: ParseException) {
        null
    }
}

fun String.toDate(): Date? {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return formatter.parse(this)
    } catch (e: ParseException) {
        null
    }
}

fun Date.addDays(day: Int): Date? {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DATE, day)
    return Date(calendar.timeInMillis)
}

fun Date.inclusiveAfter(other: Date?): Boolean {
    return other?.let {
        this.time >= other.time
    } ?: false
}
