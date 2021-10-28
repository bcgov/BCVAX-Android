package ca.bc.gov.shcdecoder.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


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

fun String.toDate(): Date? {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return formatter.parse(this)
    } catch (e: ParseException) {
        null
    }
}