package com.mparticle.kits.mobileapptracker

import android.content.Context
import com.mparticle.internal.MPUtility
import com.mparticle.internal.MPUtility.AdIdInfo
import com.mparticle.kits.KitUtils

class MATDeferredDplinkr private constructor() {
    var advertiserId: String? = null
    var conversionKey: String? = null
    var packageName: String? = null
    var googleAdvertisingId: String? = null
    private set

    var googleAdTrackingLimited = 0

    var androidId: String? = null
    var userAgent: String? = null
    var listener: MATDeeplinkListener? = null



    fun setGoogleAdvertisingId(googleAdvertisingId: String?, isLATEnabled: Int) {
        dplinkr?.googleAdvertisingId = googleAdvertisingId
        dplinkr?.googleAdTrackingLimited = isLATEnabled
    }




    fun checkForDeferredDeeplink(context: Context?, urlRequester: MATUrlRequester) {
        Thread { // If advertiser ID, conversion key, or package name were not set, return
            if (dplinkr?.advertiserId == null || dplinkr?.conversionKey == null || dplinkr?.packageName == null) {
                if (listener != null) {
                    listener?.didFailDeeplink("Advertiser ID, conversion key, or package name not set")
                }
            }
            if (dplinkr?.googleAdvertisingId == null) {
                val adIdInfo = MPUtility.getAdIdInfo(context)
                if (adIdInfo != null && adIdInfo.advertiser == AdIdInfo.Advertiser.GOOGLE) {
                    dplinkr?.googleAdvertisingId = adIdInfo.id
                    dplinkr?.googleAdTrackingLimited =
                        if (adIdInfo.isLimitAdTrackingEnabled) 1 else 0
                } else {
                    dplinkr?.androidId = KitUtils.getAndroidID(context)
                }
            }
            // If no device identifiers collected, return
            if (dplinkr?.googleAdvertisingId == null && dplinkr?.androidId == null) {
                listener?.didFailDeeplink("No device identifiers collected")
            }

            // Query for deeplink url
            dplinkr?.let { urlRequester.requestDeeplink(it) }
        }.start()
    }

    companion object {
        @Volatile
        private var dplinkr: MATDeferredDplinkr? = null
        @Synchronized
        fun initialize(
            advertiserId: String?,
            conversionKey: String?,
            packageName: String?
        ): MATDeferredDplinkr? {
            dplinkr = MATDeferredDplinkr()
            dplinkr?.advertiserId = advertiserId
            dplinkr?.conversionKey = conversionKey
            dplinkr?.packageName = packageName
            return dplinkr
        }
    }
}
