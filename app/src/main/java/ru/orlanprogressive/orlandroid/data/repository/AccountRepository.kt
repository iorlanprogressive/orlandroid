package ru.orlanprogressive.orlandroid.data.repository

import io.appmetrica.analytics.AppMetrica
import ru.orlanprogressive.orlandroid.App
import ru.orlanprogressive.orlandroid.R
import ru.orlanprogressive.orlandroid.auth.AuthManager
import ru.orlanprogressive.orlandroid.network.AppStoreApi

object AccountRepository {

	suspend fun getAccountName(): Result<String> {
		return RepositoryWrapper.wrap(
			{ account -> account.name }
		) { AppStoreApi.instance.getAccountInfo() }
	}

	suspend fun register(login: String): Result<Unit> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.register(login) }
	}

	suspend fun verifyEmail(code: String, name: String, email: String, password: String): Result<Unit> {
		return try {
			val tempApi = AppStoreApi.create(password, email)
			val response = tempApi.verifyEmail(code, name)
			if (response.isSuccessful) {
				response.body().let {
					if (it == null) {
						AppMetrica.reportError("Request error", "Response body is null")
						return Result.failure(Exception(App.instance.getString(R.string.error_request_failure)))
					}
					AuthManager.login(it.accessToken, it.issueToken, email)
				}
				Result.success(Unit)
			} else {
				AppMetrica.reportError("Request error", "Status code: ${response.code()}")
				Result.failure(Exception(App.instance.getString(
					R.string.error_request_failure_status_code,
					response.code()
				)))
			}
		} catch (e: Exception) {
			AppMetrica.reportError("Request error", "Exception", e)
			Result.failure(e)
		}
	}

	suspend fun changePassword(password: String): Result<Unit> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.changePassword(password) }
	}

	suspend fun requestEmailChange(email: String): Result<Unit> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.requestEmailChange(email) }
	}

	suspend fun changeEmail(code: String): Result<Unit> {
		return RepositoryWrapper.wrap(
			{ email -> AuthManager.editEmail(email) }
		) { AppStoreApi.instance.changeEmail(code) }
	}

	suspend fun changeName(name: String): Result<Unit> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.changeName(name) }
	}

	suspend fun requestPasswordRestore(login: String): Result<Unit> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.requestPasswordRestore(login) }
	}

	suspend fun restorePassword(code: String, email: String, password: String): Result<Unit> {
		return try {
			val tempApi = AppStoreApi.create(code, email)
			val response = tempApi.restorePassword(password)
			if (response.isSuccessful) {
				response.body().let {
					if (it == null) {
						AppMetrica.reportError("Request error", "Response body is null")
						return Result.failure(Exception(App.instance.getString(R.string.error_request_failure)))
					}
					AuthManager.login(it.accessToken, it.issueToken, email)
				}
				Result.success(Unit)
			} else {
				AppMetrica.reportError("Request error", "Status code: ${response.code()}")
				Result.failure(Exception(App.instance.getString(
					R.string.error_request_failure_status_code,
					response.code()
				)))
			}
		} catch (e: Exception) {
			AppMetrica.reportError("Request error", "Exception", e)
			Result.failure(e)
		}
	}

	suspend fun login(email: String, password: String): Result<Unit> {
		return try {
			val tempApi = AppStoreApi.create(password, email)
			val response = tempApi.login()
			if (response.isSuccessful) {
				response.body().let {
					if (it == null) {
						AppMetrica.reportError("Request error", "Response body is null")
						return Result.failure(Exception(App.instance.getString(R.string.error_request_failure)))
					}
					AuthManager.login(it.accessToken, it.issueToken, email)
				}
				Result.success(Unit)
			} else {
				AppMetrica.reportError("Request error", "Status code: ${response.code()}")
				Result.failure(Exception(App.instance.getString(
					R.string.error_request_failure_status_code,
					response.code()
				)))
			}
		} catch (e: Exception) {
			AppMetrica.reportError("Request error", "Exception", e)
			Result.failure(e)
		}
	}

	suspend fun logout(): Result<Unit> {
		return RepositoryWrapper.wrap(
			{ AuthManager.logout() }
		) { AppStoreApi.instance.logout() }
	}
}
