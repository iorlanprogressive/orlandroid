package ru.orlanprogressive.orlandroid.data.models

import com.squareup.moshi.Json

data class AppDetails(
	@param:Json(name = "Имя пакета") val packageName: String,
	@param:Json(name = "Название") val name: String,
	@param:Json(name = "Описание") val description: String?,
	@param:Json(name = "Версия") val version: String,
	@param:Json(name = "Примечания к выпуску") val changelog: String?,
	@param:Json(name = "Загружено") val publicationDate: String,
	@param:Json(name = "Обновлено") val updateDate: String,
	@param:Json(name = "Разработчик") val developer: String,
	@param:Json(name = "Статус разработчика") val developerStatus: String?,
	@param:Json(name = "Категория") val category: String,
	@param:Json(name = "Возрастное ограничение") val ageRestriction: String,
	@param:Json(name = "Рейтинг") val rating: Float?,
	@param:Json(name = "Количество пользователей") val userCount: Long
)
