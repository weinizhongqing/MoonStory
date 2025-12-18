package com.ping.night.story.fbase.fcm

import android.annotation.SuppressLint
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.messaging.FirebaseMessaging
import com.ping.night.story.BuildConfig
import com.ping.night.story.Constant
import com.ping.night.story.NsApp
import com.ping.night.story.fbase.AffairHelper
import com.ping.night.story.helper.MMKVHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object GpMessageUploadToken {

    private val TAG = "GpMessageUploadToken_LOG"
    private val okHttpClient: OkHttpClient by lazy {
        val trustAllCerts = arrayOf<TrustManager>(
            @SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
            })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }.build()
    }
    private val uploadMutex = Mutex()
    private var uploadTokenJob: Job? = null
    fun upLoadToken() {
        if (uploadTokenJob?.isActive == true) return
        uploadTokenJob = NsApp.scope.launch {
            uploadMutex.withLock {
                val token = getToken() ?: return@launch
                val lastToken = MMKVHelper.getToken()
                if (token == lastToken && MMKVHelper.getRpFcmDally()) return@launch
                AffairHelper.instance.event("ns_start_upload_token")
                var gaid = MMKVHelper.getGaid()
                if (gaid.isEmpty()) {
                    gaid = getGoogleAdvertisingId()
                    MMKVHelper.setGaid(gaid)
                }
//                Log.d(TAG, "gaid====: $gaid")
//                Log.d(TAG, "token====: $token")
                val payloadJson = JSONObject().apply {
                    put("lum", gaid)
                    put("kui", MMKVHelper.appFirstOpenTime.toString())
                    put("p0e", token)
                    put("attrs", MMKVHelper.plyRefStr)
                }
                val request = Request.Builder()
                    .url(Constant.UPLOAD_TOKEN_URL_KEY)
                    .post(payloadJson.toString().toRequestBody("application/json".toMediaType()))
                    .addHeader("VI", NsApp.app.packageName)
                    .addHeader("VU", BuildConfig.VERSION_NAME)
                    .addHeader("Content-Type", "application/json")
                    .build()
                var attempts = 0
                var success = false
                var lastError = ""
                while (attempts++ < 3 && !success) {
                    val response = try {
                        okHttpClient.newCall(request).execute()
                    } catch (e: Exception) {
                        lastError = e.message ?: "Unknown error"
                        null
                    }
                    if (response?.isSuccessful == true) {
                        val responseBody = response.body?.string()
                        Log.d(TAG, "Upload token successful: $responseBody")
                        MMKVHelper.setRpFcmDally()
                        MMKVHelper.setToken(token)
                        AffairHelper.instance.event("ns_success_upload_token")
                        success = true
                    } else {
                        lastError = "${response?.code} -> ${response?.message}"
                        if (attempts < 3) delay(10_000)
                    }
                }

                if (!success) {
                    Log.d(TAG, "Upload token failed after 3 attempts: $lastError")
                    AffairHelper.instance.event("ns_success_upload_fail", "msg", lastError)
                }
            }
        }

    }
    suspend fun getToken(): String? = suspendCoroutine { cont ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            cont.resume(if (it.isSuccessful) it.result else null)
        }
    }
    suspend fun getGoogleAdvertisingId(): String {
        return try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(NsApp.app)
            adInfo.id ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    
}