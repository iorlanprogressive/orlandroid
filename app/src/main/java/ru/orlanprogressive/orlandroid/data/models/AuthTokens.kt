package ru.orlanprogressive.orlandroid.data.models

import com.squareup.moshi.Json

data class AuthTokens(
	@param:Json(name = "Токен доступа") val accessToken: String,
	@param:Json(name = "Токен выпуска") val issueToken: String
)
