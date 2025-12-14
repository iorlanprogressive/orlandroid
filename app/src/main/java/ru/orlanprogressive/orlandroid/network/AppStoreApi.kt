package ru.orlanprogressive.orlandroid.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import ru.orlanprogressive.orlandroid.auth.AuthManager
import ru.orlanprogressive.orlandroid.data.models.Account
import ru.orlanprogressive.orlandroid.data.models.AppDetails
import ru.orlanprogressive.orlandroid.data.models.Contact
import ru.orlanprogressive.orlandroid.data.models.Developer
import ru.orlanprogressive.orlandroid.data.models.Review
import ru.orlanprogressive.orlandroid.data.models.AppPreview
import ru.orlanprogressive.orlandroid.data.models.AuthTokens
import ru.orlanprogressive.orlandroid.data.models.MyApp
import ru.orlanprogressive.orlandroid.data.models.MyReview

interface AppStoreApi {

	companion object {

		const val BASE_URL = "https://api.orlan-progressive.ru/"

		private const val ACCEPT_CHARSET = "Accept-Charset: utf-8"
		private const val ACCEPT_JSON = "Accept: application/json"
		private const val ACCEPT_PNG = "Accept: image/png"

		private var _instance: AppStoreApi? = null
			get() = field ?: create().also { field = it }
		val instance get() = _instance!!

		fun create(
			secret: String? = AuthManager.getAccessToken(),
			username: String? = AuthManager.getEmail()
		): AppStoreApi {
			val client = OkHttpClient.Builder()
				.addInterceptor(AuthInterceptor(secret, username))
				.build()

			val retrofit = Retrofit.Builder()
				.baseUrl(BASE_URL)
				.client(client)
				.addConverterFactory(MoshiConverterFactory.create(
					Moshi.Builder()
						.add(KotlinJsonAdapterFactory())
						.build()
				))
				.build()

			return retrofit.create(AppStoreApi::class.java)
		}

		fun setCredentials(
			secret: String? = AuthManager.getAccessToken(),
			username: String? = AuthManager.getEmail()
		) {
			_instance = create(secret, username)
		}
	}

	@GET("apps/top/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun getTopApps(
		@Query("сортировка") sort: Int,
		@Query("количество") count: Int,
		@Query("смещение") offset: Int
	): Response<List<AppPreview>>

	@GET("apps/category/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun getAppsByCategory(
		@Query("категория") category: String,
		@Query("сортировка") sort: Int,
		@Query("количество") count: Int,
		@Query("смещение") offset: Int
	): Response<List<AppPreview>>

	@GET("apps/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun searchApps(
		@Query("запрос") query: String,
		@Query("сортировка") sort: Int,
		@Query("количество") count: Int,
		@Query("смещение") offset: Int
	): Response<List<AppPreview>>

	@GET("app/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun getAppInfo(@Query("пакет") packageName: String): Response<AppDetails>

	@HEAD("app/screenshot/")
	@Headers(ACCEPT_PNG)
	suspend fun checkAppScreenshotExists(
		@Query("пакет") packageName: String,
		@Query("скриншот") screenshotId: Int
	): Response<Unit>

	@GET("app/reviews/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun getAppReviews(
		@Query("пакет") packageName: String,
		@Query("количество") count: Int,
		@Query("смещение") offset: Int
	): Response<List<Review>>

	@GET("app/reviews/my/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun getMyReview(@Query("пакет") packageName: String): Response<MyReview>

	@POST("app/reviews/my/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	@FormUrlEncoded
	suspend fun leaveReview(
		@Query("пакет") packageName: String,
		@Field("рейтинг") rating: Int,
		@Field("текст") text: String?
	): Response<Unit>

	@PUT("app/reviews/my/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	@FormUrlEncoded
	suspend fun editReview(
		@Query("пакет") packageName: String,
		@Field("рейтинг") rating: Int,
		@Field("текст") text: String?
	): Response<Unit>

	@DELETE("app/reviews/my/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun deleteReview(@Query("пакет") packageName: String): Response<Unit>

	@POST("app/reviews/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	@FormUrlEncoded
	suspend fun rateReview(
		@Query("отзыв") reviewId: Long,
		@Field("оценка") rate: Int
	): Response<Unit>

	@PUT("app/reviews/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	@FormUrlEncoded
	suspend fun editReviewRating(
		@Query("отзыв") reviewId: Long,
		@Field("оценка") rate: Int
	): Response<Unit>

	@DELETE("app/reviews/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun deleteReviewRating(@Query("отзыв") reviewId: Long): Response<Unit>

	@GET("developer/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun getDeveloperInfo(@Query("разработчик") developerName: String): Response<Developer>

	@GET("developer/sites/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun getDeveloperSites(@Query("разработчик") developerName: String): Response<List<Contact>>

	@GET("developer/emails/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun getDeveloperEmails(@Query("разработчик") developerName: String): Response<List<Contact>>

	@GET("developer/telephones/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun getDeveloperPhones(@Query("разработчик") developerName: String): Response<List<Contact>>

	@GET("developer/apps/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun getDeveloperApps(
		@Query("разработчик") developerName: String,
		@Query("сортировка") sort: Int,
		@Query("количество") count: Int,
		@Query("смещение") offset: Int
	): Response<List<AppPreview>>

	@GET("account/apps/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun getMyApps(): Response<List<MyApp>>

	@DELETE("account/apps/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun removeFromMyApps(@Query("пакет") packageName: String): Response<Unit>

	@GET("account/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun getAccountInfo(): Response<Account>

	@POST("account/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	@FormUrlEncoded
	suspend fun register(@Field("логин") login: String): Response<Unit>

	@PUT("account/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	@FormUrlEncoded
	suspend fun verifyEmail(
		@Field("код") code: String,
		@Field("имя") name: String
	): Response<AuthTokens>

	@PATCH("account/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	@FormUrlEncoded
	suspend fun changePassword(@Field("пароль") password: String): Response<Unit>

	@POST("account/edit/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	@FormUrlEncoded
	suspend fun requestEmailChange(@Field("логин") login: String): Response<Unit>

	@PUT("account/edit/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	@FormUrlEncoded
	suspend fun changeEmail(@Field("код") code: String): Response<String>

	@PATCH("account/edit/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	@FormUrlEncoded
	suspend fun changeName(@Field("имя") name: String): Response<Unit>

	@POST("account/restore/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	@FormUrlEncoded
	suspend fun requestPasswordRestore(@Field("логин") login: String): Response<Unit>

	@PUT("account/restore/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	@FormUrlEncoded
	suspend fun restorePassword(@Field("пароль") password: String): Response<AuthTokens>

	@POST("account/session/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun login(): Response<AuthTokens>

	@PUT("account/session/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun refreshTokens(): Response<AuthTokens>

	@DELETE("account/session/")
	@Headers(ACCEPT_CHARSET, ACCEPT_JSON)
	suspend fun logout(): Response<Unit>
}
