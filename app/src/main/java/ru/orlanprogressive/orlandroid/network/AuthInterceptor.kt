package ru.orlanprogressive.orlandroid.network

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
	private val secret: String?,
	private val username: String?
) : Interceptor {

	override fun intercept(chain: Interceptor.Chain): Response {
		val originalRequest = chain.request()

		if (secret != null && username != null) {
			val request = originalRequest.newBuilder()
				.header("Authorization", "Basic " + Base64.encodeToString(
					"$username:$secret".toByteArray(),
					Base64.NO_WRAP
				))
				.build()

			val response =  chain.proceed(request)

			return response
		}

		return chain.proceed(originalRequest)
	}
}
