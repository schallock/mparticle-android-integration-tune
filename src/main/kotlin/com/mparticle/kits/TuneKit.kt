package com.mparticle.kits

import android.content.Context
import com.mparticle.kits.KitIntegration
import com.mparticle.kits.mobileapptracker.MATDeeplinkListener
import com.mparticle.kits.KitIntegration.ApplicationStateListener
import com.mparticle.kits.mobileapptracker.MATDeferredDplinkr
import java.util.concurrent.atomic.AtomicBoolean
import com.mparticle.kits.ReportingMessage
import com.mparticle.kits.mobileapptracker.MATUtils
import com.mparticle.kits.TuneKit
import com.mparticle.kits.KitUtils
import com.mparticle.kits.mobileapptracker.MATUrlRequester
import com.mparticle.AttributionResult
import com.mparticle.AttributionError

/**
 * Tune Kit implementing Tune's post-install deep-link feature. Different from other Kits, the Tune Kit
 * does not actually wrap the full Tune SDK - only a small subset of classes required to query the Tune server
 * for deep links that match the given user.
 */
class TuneKit : KitIntegration(), MATDeeplinkListener, ApplicationStateListener {
    private var settingAdvertiserId: String? = null
    private var settingConversionKey: String? = null
    var packageName: String? = null
    private var deepLinker: MATDeferredDplinkr? = null
    private val listenerWaiting = AtomicBoolean(false)
    override fun getName(): String = KIT_NAME

    override fun onKitCreate(
        settings: Map<String, String>,
        context: Context
    ): List<ReportingMessage> {
        if (MATUtils.firstInstall(getContext())) {
            settingAdvertiserId = getSettings()[SETTING_ADVERTISER_ID]
            settingConversionKey = getSettings()[SETTING_CONVERSION_KEY]
            packageName = getSettings()[SETTING_PACKAGE_NAME_OVERRIDE]
            if (KitUtils.isEmpty(packageName)) {
                packageName = getContext().packageName
            }
            deepLinker = MATDeferredDplinkr.initialize(
                settingAdvertiserId,
                settingConversionKey,
                packageName
            )
            deepLinker?.listener = this
            checkForAttribution()
        }
        return emptyList()
    }

    override fun setOptOut(optedOut: Boolean): List<ReportingMessage> = emptyList()

    fun setUserAgent(userAgent: String?) {
        deepLinker?.let {
            it.userAgent = userAgent
            if (listenerWaiting.get()) {
                checkForAttribution()
            }
        }
    }

    private fun checkForAttribution() {
        deepLinker?.let { deepLinker ->
            listenerWaiting.set(true)
            if (deepLinker.userAgent == null) {
                MATUtils.calculateUserAgent(context, this)
            } else {
                deepLinker.checkForDeferredDeeplink(context, MATUrlRequester())
            }
        }
    }

    override fun didReceiveDeeplink(deeplink: String?) {
        listenerWaiting.set(false)
        val result = AttributionResult()
            .setLink(deeplink)
            .setServiceProviderId(configuration.kitId)
        kitManager.onResult(result)
    }

    override fun didFailDeeplink(error: String?) {
        listenerWaiting.set(false)
        val deepLinkError = AttributionError()
            .setMessage(error)
            .setServiceProviderId(configuration.kitId)
        kitManager.onError(deepLinkError)
    }

    override fun onApplicationForeground() {
        checkForAttribution()
    }

    override fun onApplicationBackground() {}

    companion object {
        private const val SETTING_ADVERTISER_ID = "advertiserId"
        private const val SETTING_CONVERSION_KEY = "conversionKey"
        private const val SETTING_PACKAGE_NAME_OVERRIDE = "overridePackageName"
        private const val KIT_NAME = "Tune"
    }
}