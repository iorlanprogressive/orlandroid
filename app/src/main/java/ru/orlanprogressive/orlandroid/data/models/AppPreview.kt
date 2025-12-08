package ru.orlanprogressive.orlandroid.data.models

import com.squareup.moshi.Json

data class AppPreview(
	@param:Json(name = "Имя пакета") val packageName: String,
	@param:Json(name = "Название") val name: String,
	@param:Json(name = "Разработчик") val developer: String,
	@param:Json(name = "Категория") val category: String,
	@param:Json(name = "Возрастное ограничение") val ageRestriction: String,
	@param:Json(name = "Рейтинг") val rating: Float?,
	@param:Json(name = "Количество пользователей") val userCount: Long
)
