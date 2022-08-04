package com.mparticle.kits.mobileapptracker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.WebSettings
import android.webkit.WebView
import com.mparticle.kits.KitUtils
import com.mparticle.kits.TuneKit
import java.io.*
import java.lang.ref.WeakReference

object MATUtils {
    /**
     * Reads an InputStream and converts it to a String
     * @param stream InputStream to read
     * @return String of stream contents
     * @throws IOException Reader was closed when trying to be read
     * @throws UnsupportedEncodingException UTF-8 encoding could not be found
     */
    @Throws(IOException::class, UnsupportedEncodingException::class)
    fun readStream(stream: InputStream?): String {
        if (stream != null) {
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            val builder = StringBuilder()
            var line: String? = null
            while (reader.readLine().also { line = it } != null) {
                builder.append(line).append("\n")
            }
            reader.close()
            return builder.toString()
        }
        return ""
    }

    /**
     * Determine the device's user agent and set the corresponding field.
     */
    fun calculateUserAgent(context: Context?, tuneKit: TuneKit) {
        val userAgent = System.getProperty("http.agent", "")
        if (!KitUtils.isEmpty(userAgent)) {
            tuneKit.setUserAgent(userAgent)
        } else {
            // If system doesn't have user agent,
            // execute Runnable on UI thread to get WebView user agent
            val handler = Handler(Looper.getMainLooper())
            handler.post(GetWebViewUserAgent(context, tuneKit))
        }
    }

    fun firstInstall(context: Context): Boolean {
        // If SharedPreferences value for install exists, set firstInstall false
        val installed = context.getSharedPreferences(MATConstants.PREFS_TUNE, Context.MODE_PRIVATE)
        return if (installed.contains(MATConstants.KEY_INSTALL)) {
            false
        } else {
            // Set install value in SharedPreferences if not there
            installed.edit().putBoolean(MATConstants.KEY_INSTALL, true).apply()
            true
        }
    }

    /**
     * Runnable for getting the WebView user agent
     */
    @SuppressLint("NewApi")
    private class GetWebViewUserAgent(context: Context?, tuneKit: TuneKit) : Runnable {
        private val weakContext: WeakReference<Context?>
        private val tuneKit: TuneKit
        override fun run() {
            try {
                Class.forName("android.os.AsyncTask") // prevents WebView from crashing on certain devices
                if (Build.VERSION.SDK_INT >= 17) {
                    tuneKit.setUserAgent(WebSettings.getDefaultUserAgent(weakContext.get()))
                } else {
                    // Create WebView to set user agent, then destroy WebView
                    val wv = WebView(weakContext.get()!!)
                    tuneKit.setUserAgent(wv.settings.userAgentString)
                    wv.destroy()
                }
            } catch (e: Exception) {
                // Alcatel has WebView implementation that causes getDefaultUserAgent to NPE
                // Reference: https://groups.google.com/forum/#!topic/google-admob-ads-sdk/SX9yb3F_PNk
            } catch (e: VerifyError) {
                // Some device vendors have their own WebView implementation which crashes on our init
            }
        }

        init {
            weakContext = WeakReference(context)
            this.tuneKit = tuneKit
        }
    }
}