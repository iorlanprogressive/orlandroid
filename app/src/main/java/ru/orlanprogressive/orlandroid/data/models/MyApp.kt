package ru.orlanprogressive.orlandroid.data.models

import com.squareup.moshi.Json

data class MyApp(
	@param:Json(name = "Имя пакета") val packageName: String,
	@param:Json(name = "Название") val name: String
)
