package com.example.musicplatform.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.musicplatform.model.AuthResponse
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
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class ApiClient(private val context: Context) {

    private val BASE_URL = "http://192.168.117.15:8080"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = getJwtToken(context)

                val requestBuilder = chain.request().newBuilder()
                token?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }

                chain.proceed(requestBuilder.build())
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

    val trackApiService: TrackApiService by lazy {
        retrofit.create(TrackApiService::class.java)
    }

    val playlistApiService: PlaylistApiService by lazy {
        retrofit.create(PlaylistApiService::class.java)
    }

    val artistApiService: ArtistApiService by lazy {
        retrofit.create(ArtistApiService::class.java)
    }

    val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val recommendationApiService: RecommendationApiService by lazy {
        retrofit.create(RecommendationApiService::class.java)
    }

    fun login(
        username: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit,
        context: Context,
    ) {
        val url = "$BASE_URL/auth/login"
        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Error: Unable to reach the server")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                    try {
                        val authResponse = Gson().fromJson(responseData, AuthResponse::class.java)
                        saveToken(authResponse.token, context)
                        saveUser(authResponse.user, context)
                        CoroutineScope(Dispatchers.Main).launch { onSuccess(authResponse.user) }
                    } catch (e: JSONException) {
                        onError("Error: Incorrect response from the server")
                    }
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
                    CoroutineScope(Dispatchers.Main).launch { onError(errorMessage) }
                }
            }
        })
    }

    fun register(
        email: String,
        username: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit,
        context: Context
    ) {
        val url = "$BASE_URL/auth/register"
        val json = JSONObject().apply {
            put("email", email)
            put("username", username)
            put("password", password)
        }

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Error: Unable to reach the server")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (!responseData.isNullOrEmpty()) {
                        try {
                            val authResponse =
                                Gson().fromJson(responseData, AuthResponse::class.java)
                            saveToken(authResponse.token, context)
                            saveUser(authResponse.user, context)
                            CoroutineScope(Dispatchers.Main).launch {
                                onSuccess(authResponse.user)
                            }
                        } catch (e: JSONException) {
                            onError("Error: Failed to parse JSON response")
                        }
                    } else {
                        onError("Error: Empty response from server")
                    }
                } else {
                    val responseData = response.body?.string()
                    if (!responseData.isNullOrEmpty()) {
                        try {
                            val jsonObject = JSONObject(responseData)
                            val errorMessage =
                                jsonObject.optString("message", "Unknown error occurred")
                            onError(errorMessage)
                        } catch (e: JSONException) {
                            onError("Error: Failed to parse error response")
                        }
                    } else {
                        onError("Error: Unknown error occurred")
                    }
                }
            }
        })
    }

    fun requestEmailVerification(
        userId: Long,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$BASE_URL/auth/verification?userId=$userId"

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
        userId: Long,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$BASE_URL/auth/check-verification?userId=$userId"
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

    fun verifyEmail(token: String, userId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val url = "$BASE_URL/auth/confirm?token=$token&userId=$userId"
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

    fun logout(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$BASE_URL/auth/logout"
        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(null, ""))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    onError("Error: Unable to connect to the server")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (response.isSuccessful) {
                    Handler(Looper.getMainLooper()).post {
                        clearToken(context)
                        clearUser(context)
                        onSuccess()
                    }
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
                    Handler(Looper.getMainLooper()).post {
                        onError(errorMessage)
                    }
                }
            }
        })
    }

    fun saveToken(token: String, context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("jwt_token", token).apply()
    }

    fun saveUser(user: User, context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        user.id?.let {
            sharedPreferences.edit()
                .putLong("user_id", it)
                .putString("user_email", user.email)
                .putString("user_username", user.email)
                .putString("user_password", user.email)
                .putString("user_role", user.roles)
                .putBoolean("user_email_verified", user.emailVerified)
                .apply()
        }
    }

    fun getJwtToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }

    fun getUser(context: Context): User {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val user = User(null, "", "", "", "", false, null)
        user.id = sharedPreferences.getLong("user_id", 0)
        user.email = sharedPreferences.getString("user_email", "").toString()
        user.username = sharedPreferences.getString("user_username", "").toString()
        user.password = sharedPreferences.getString("user_password", "").toString()
        user.roles = sharedPreferences.getString("user_role", "").toString()
        user.emailVerified = sharedPreferences.getBoolean("user_email_verified", false)
        return user
    }

    fun clearToken(context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("jwt_token").apply()
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
