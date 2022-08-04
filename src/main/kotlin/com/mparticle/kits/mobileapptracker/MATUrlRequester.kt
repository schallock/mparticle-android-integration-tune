package com.mparticle.kits.mobileapptracker

import android.net.Uri
import com.mparticle.internal.Logger
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MATUrlRequester {
    fun requestDeeplink(dplinkr: MATDeferredDplinkr) {
        var deeplink: String? = ""
        var `is`: InputStream? = null

        // Construct deeplink endpoint url
        val uri = Uri.Builder()
        uri.scheme("https")
            .authority(dplinkr.advertiserId + "." + MATConstants.DEEPLINK_DOMAIN)
            .appendPath("v1")
            .appendPath("link.txt")
            .appendQueryParameter("platform", "android")
            .appendQueryParameter("advertiser_id", dplinkr.advertiserId)
            .appendQueryParameter("ver", MATConstants.SDK_VERSION)
            .appendQueryParameter("package_name", dplinkr.packageName)
            .appendQueryParameter(
                "ad_id",
                if (dplinkr.googleAdvertisingId != null) dplinkr.googleAdvertisingId else dplinkr.androidId
            )
            .appendQueryParameter("user_agent", dplinkr.userAgent)
        if (dplinkr.googleAdvertisingId != null) {
            uri.appendQueryParameter(
                "google_ad_tracking_disabled",
                dplinkr.googleAdTrackingLimited.toString()
            )
        }
        try {
            val myurl = URL(uri.build().toString())
            val conn = myurl.openConnection() as HttpURLConnection
            // Set TUNE conversion key in request header
            conn.setRequestProperty("X-MAT-Key", dplinkr.conversionKey)
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.connect()
            var error = false
            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                `is` = conn.inputStream
            } else {
                error = true
                `is` = conn.errorStream
            }
            deeplink = MATUtils.readStream(`is`)
            val listener = dplinkr.listener
            if (listener != null) {
                if (error) {
                    // Notify listener of error
                    listener.didFailDeeplink(deeplink)
                } else {
                    // Notify listener of deeplink url
                    listener.didReceiveDeeplink(deeplink)
                }
            }
        } catch (e: Exception) {
            Logger.debug(e, "Error while querying Tune web service for deep links.")
        } finally {
            try {
                `is`?.close()
            } catch (e: Exception) {
                Logger.debug(e, "Error while querying Tune web service for deep links.")
            }
        }
    }
}
