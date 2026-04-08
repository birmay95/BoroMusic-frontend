package com.example.musicplatform.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.musicplatform.model.AuthResponse
import com.example.musicplatform.model.TokenRefreshRequest
import com.example.musicplatform.model.User
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.UUID

class ApiClient(private val context: Context) {

    private val BASE_URL = "http://192.168.185.15:8080"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val accessToken = getAccessToken(context)

                val requestBuilder = originalRequest.newBuilder()
                accessToken?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }

                var response = chain.proceed(requestBuilder.build())

                if (response.code == 401) {
                    synchronized(this) {
                        val currentToken = getAccessToken(context)

                        if (currentToken != null && currentToken != accessToken) {
                            response.close()
                            val retriedRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer $currentToken")
                                .build()
                            return@addInterceptor chain.proceed(retriedRequest)
                        }

                        val newAccessToken = refreshAccessTokenSynchronously()

                        if (newAccessToken != null) {
                            response.close()
                            val retriedRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer $newAccessToken")
                                .build()
                            response = chain.proceed(retriedRequest)
                        } else {
                            clearTokens(context)
                            clearUser(context)
                        }
                    }
                }
                response
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val trackApiService: TrackApiService by lazy { retrofit.create(TrackApiService::class.java) }
    val playlistApiService: PlaylistApiService by lazy { retrofit.create(PlaylistApiService::class.java) }
    val artistApiService: ArtistApiService by lazy { retrofit.create(ArtistApiService::class.java) }
    val userApiService: UserApiService by lazy { retrofit.create(UserApiService::class.java) }
    val recommendationApiService: RecommendationApiService by lazy { retrofit.create(RecommendationApiService::class.java) }

    private fun refreshAccessTokenSynchronously(): String? {
        val refreshToken = getRefreshToken(context) ?: return null
        val url = "$BASE_URL/api/v1/auth/refresh"

        val json = JSONObject().apply { put("refreshToken", refreshToken) }
        val body =
            json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder().url(url).post(body).build()

        val basicClient = OkHttpClient()

        return try {
            val response = basicClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseData = response.body?.string()
                val authResponse = Gson().fromJson(responseData, AuthResponse::class.java)
                saveTokens(authResponse.accessToken, authResponse.refreshToken, context)
                authResponse.accessToken
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun login(username: String, password: String, onSuccess: (User) -> Unit, onError: (String) -> Unit, context: Context) {
        val url = "$BASE_URL/api/v1/auth/login"
        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val body =
            json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder().url(url).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Error: Unable to reach the server")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                    try {
                        val authResponse = Gson().fromJson(responseData, AuthResponse::class.java)
                        saveTokens(authResponse.accessToken, authResponse.refreshToken, context)
                        saveUser(authResponse.user, context)
                        CoroutineScope(Dispatchers.Main).launch { onSuccess(authResponse.user) }
                    } catch (e: JSONException) {
                        onError("Error: Incorrect response from the server")
                    }
                } else {
                    val errorMessage = try {
                        val jsonObject = JSONObject(responseData ?: "")
                        jsonObject.optString("message", "Error: ${response.code} ${response.message}")
                    } catch (e: Exception) {
                        "Error: ${response.code} ${response.message}"
                    }
                    CoroutineScope(Dispatchers.Main).launch { onError(errorMessage) }
                }
            }
        })
    }

    fun register(email: String, username: String, password: String, onSuccess: (User) -> Unit, onError: (String) -> Unit, context: Context) {
        val url = "$BASE_URL/api/v1/auth/register"
        val json = JSONObject().apply {
            put("email", email)
            put("username", username)
            put("password", password)
        }

        val body =
            json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder().url(url).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Error: Unable to reach the server")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                    try {
                        val authResponse = Gson().fromJson(responseData, AuthResponse::class.java)
                        saveTokens(authResponse.accessToken, authResponse.refreshToken, context)
                        saveUser(authResponse.user, context)
                        CoroutineScope(Dispatchers.Main).launch { onSuccess(authResponse.user) }
                    } catch (e: JSONException) {
                        onError("Error: Failed to parse JSON response")
                    }
                } else {
                    val errorMessage = try {
                        val jsonObject = JSONObject(responseData ?: "")
                        jsonObject.optString("message", "Unknown error occurred")
                    } catch (e: Exception) {
                        "Error: ${response.code} ${response.message}"
                    }
                    CoroutineScope(Dispatchers.Main).launch { onError(errorMessage) }
                }
            }
        })
    }

    fun requestEmailVerification(
        userId: UUID,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$BASE_URL/api/v1/auth/verification/$userId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Error: Unable to connect to the server")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (response.isSuccessful) {
                    onSuccess(responseData ?: "Email verification request sent")
                } else {
                    val errorMessage = try {
                        val jsonObject = JSONObject(responseData ?: "")
                        jsonObject.optString(
                            "message",
                            "Error: ${response.code} ${response.message}"
                        )
                    } catch (e: Exception) {
                        "Error: ${response.code} ${response.message}"
                    }
                    onError(errorMessage)
                }
            }
        })
    }

    fun checkEmailVerification(
        userId: UUID,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$BASE_URL/api/v1/auth/check-verification/$userId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Error: Unable to connect to the server")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (response.isSuccessful) {
                    val isVerified = responseData?.toBoolean() ?: false
                    onSuccess(isVerified)
                } else {
                    val errorMessage = try {
                        val jsonObject = JSONObject(responseData ?: "")
                        jsonObject.optString(
                            "message",
                            "Error: ${response.code} ${response.message}"
                        )
                    } catch (e: Exception) {
                        "Error: ${response.code} ${response.message}"
                    }
                    onError(errorMessage)
                }
            }
        })
    }

    fun verifyEmail(token: String, userId: UUID, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val url = "$BASE_URL/api/v1/auth/confirm/$userId"

        val json = JSONObject().apply {
            put("token", token)
        }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Error: Unable to connect to the server")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorMessage = try {
                        val jsonObject = JSONObject(responseData ?: "")
                        jsonObject.optString(
                            "message",
                            "Error: ${response.code} ${response.message}"
                        )
                    } catch (e: Exception) {
                        "Error: ${response.code} ${response.message}"
                    }
                    onError(errorMessage)
                }
            }
        })
    }

    fun logout(userId: UUID, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val url = "$BASE_URL/api/v1/auth/logout/$userId"
        val request = Request.Builder()
            .url(url)
            .post("".toRequestBody(null))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    onError("Error: Unable to connect to the server")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Handler(Looper.getMainLooper()).post {
                        clearTokens(context)
                        clearUser(context)
                        onSuccess()
                    }
                } else {
                    val errorMessage = try {
                        val jsonObject = JSONObject(response.body?.string() ?: "")
                        jsonObject.optString("message", "Error: ${response.code}")
                    } catch (e: Exception) {
                        "Error: Logout failed"
                    }
                    Handler(Looper.getMainLooper()).post {
                        clearTokens(context)
                        clearUser(context)
                        onError(errorMessage)
                    }
                }
            }
        })
    }


    fun saveTokens(accessToken: String, refreshToken: String, context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun getAccessToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("access_token", null)
    }

    fun getRefreshToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("refresh_token", null)
    }

    fun clearTokens(context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .remove("access_token")
            .remove("refresh_token")
            .apply()
    }

    fun saveUser(user: User, context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        user.id?.let {
            sharedPreferences.edit()
                .putString("user_id", it.toString())
                .putString("user_email", user.email)
                .putString("user_username", user.username)
                .putString("user_password", user.password)
                .putString("user_role", user.roles)
                .putBoolean("user_email_verified", user.emailVerified)
                .apply()
        }
    }

    fun getUser(context: Context): User {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val user = User(null, "", "", "", "", false, null)
        val userId: UUID? = sharedPreferences.getString("user_id", null)?.let {
            kotlin.runCatching { UUID.fromString(it) }.getOrNull()
        }
        user.id = userId
        user.email = sharedPreferences.getString("user_email", "").toString()
        user.username = sharedPreferences.getString("user_username", "").toString()
        user.password = sharedPreferences.getString("user_password", "").toString()
        user.roles = sharedPreferences.getString("user_role", "").toString()
        user.emailVerified = sharedPreferences.getBoolean("user_email_verified", false)
        return user
    }

    fun clearUser(context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("user_id").apply()
        sharedPreferences.edit().remove("user_email").apply()
        sharedPreferences.edit().remove("user_username").apply()
        sharedPreferences.edit().remove("user_password").apply()
        sharedPreferences.edit().remove("user_role").apply()
        sharedPreferences.edit().remove("user_artist_request").apply()
    }
}