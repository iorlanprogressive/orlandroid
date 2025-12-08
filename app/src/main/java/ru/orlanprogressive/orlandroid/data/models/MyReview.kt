package ru.orlanprogressive.orlandroid.data.models

import com.squareup.moshi.Json

data class MyReview(
	@param:Json(name = "Рейтинг") val rating: Int,
	@param:Json(name = "Текст") val text: String?,
	@param:Json(name = "Дата и время отзыва") val date: String?,
	@param:Json(name = "Обратная связь") val feedback: String?,
	@param:Json(name = "Дата и время обратной связи") val feedbackDate: String?,
	@param:Json(name = "Количество оценок \"Нравится\"") val likes: Int,
	@param:Json(name = "Количество оценок \"Не нравится\"") val dislikes: Int,
)
