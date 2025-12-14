package ru.orlanprogressive.orlandroid.data.models

import com.squareup.moshi.Json

data class Developer(
	@param:Json(name = "Описание") val description: String,
	@param:Json(name = "Статус") val status: String?
)
