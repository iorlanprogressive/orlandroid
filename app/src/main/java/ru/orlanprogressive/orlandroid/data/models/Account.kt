package ru.orlanprogressive.orlandroid.data.models

import com.squareup.moshi.Json

data class Account(
	@param:Json(name = "Имя") val name: String
)
