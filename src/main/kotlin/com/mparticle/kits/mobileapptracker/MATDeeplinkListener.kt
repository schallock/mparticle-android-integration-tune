package com.mparticle.kits.mobileapptracker

interface MATDeeplinkListener {
    fun didReceiveDeeplink(deeplink: String?)
    fun didFailDeeplink(error: String?)
}