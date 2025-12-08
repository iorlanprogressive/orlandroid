package ru.orlanprogressive.orlandroid.data.models

import com.squareup.moshi.Json

data class Contact(
	@param:Json(name = "Контакт") val link: String,
	@param:Json(name = "Примечание") val label: String?
)
