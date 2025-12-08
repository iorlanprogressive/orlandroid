package ru.orlanprogressive.orlandroid.data.repository

import io.appmetrica.analytics.AppMetrica
import retrofit2.Response
import ru.orlanprogressive.orlandroid.App
import ru.orlanprogressive.orlandroid.R
import ru.orlanprogressive.orlandroid.auth.AuthManager
import ru.orlanprogressive.orlandroid.network.AppStoreApi
import kotlin.collections.emptyList

object RepositoryWrapper {

	suspend inline fun <reified T, R> wrap(
		noinline interceptor: ((T) -> R)? = null,
		request: suspend () -> Response<T>
	): Result<R> {
		return try {
			var response = request()
			if (!response.isSuccessful && response.code() == 401 && AuthManager.isLoggedIn()) {
				val api = AppStoreApi.create(AuthManager.getIssueToken())
				val refreshResponse = api.refreshTokens()
				if (refreshResponse.isSuccessful) {
					refreshResponse.body()?.also {
						AuthManager.refreshTokens(it.accessToken, it.issueToken)
						response = request()
					}
				} else {
					AppMetrica.reportError("Request error", "Status code: ${response.code()}")
					return Result.failure(
						Exception(
							App.instance.getString(
								R.string.error_request_failure_status_code,
								response.code()
							)
						)
					)
				}
			}
			when {
				response.isSuccessful -> if (T::class == Unit::class) {
					val result = Unit as T
					@Suppress("UNCHECKED_CAST")
					Result.success(if (interceptor != null) interceptor(result) else result as R)
				} else {
					response.body().let { body ->
						@Suppress("UNCHECKED_CAST")
						if (body == null) {
							AppMetrica.reportError("Request error", "Response body is null")
							Result.failure(Exception(App.instance.getString(R.string.error_request_failure)))
						} else {
							Result.success(if (interceptor != null) interceptor(body) else body as R)
						}
					}
				}
				response.code() == 404 && T::class.java.isAssignableFrom(List::class.java) -> {
					val emptyList = emptyList<Any>() as T
					@Suppress("UNCHECKED_CAST")
					Result.success(if (interceptor != null) interceptor(emptyList) else emptyList as R)
				}
				else -> {
					AppMetrica.reportError("Request error", "Status code: ${response.code()}")
					Result.failure(Exception(App.instance.getString(
						R.string.error_request_failure_status_code,
						response.code()
					)))
				}
			}
		} catch (e: Exception) {
			AppMetrica.reportError("Request error", "Exception", e)
			Result.failure(e)
		}
	}

	suspend inline fun <reified T> wrap(request: suspend () -> Response<T>): Result<T> {
		return wrap(null, request)
	}
}
