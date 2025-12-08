package ru.orlanprogressive.orlandroid.data.repository

import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.orlanprogressive.orlandroid.data.models.AppDetails
import ru.orlanprogressive.orlandroid.data.models.AppPreview
import ru.orlanprogressive.orlandroid.data.models.Contact
import ru.orlanprogressive.orlandroid.data.models.Developer
import ru.orlanprogressive.orlandroid.data.models.DeveloperData
import ru.orlanprogressive.orlandroid.data.models.MyApp
import ru.orlanprogressive.orlandroid.data.models.MyReview
import ru.orlanprogressive.orlandroid.data.models.Review
import ru.orlanprogressive.orlandroid.network.AppStoreApi

object AppRepository {

	suspend fun getTopApps(sort: Int, count: Int, offset: Int): Result<List<AppPreview>> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.getTopApps(sort, count, offset) }
	}

	suspend fun getAppsByCategory(category: String, sort: Int, count: Int, offset: Int): Result<List<AppPreview>> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.getAppsByCategory(category, sort, count, offset) }
	}

	suspend fun searchApps(query: String, sort: Int, count: Int, offset: Int): Result<List<AppPreview>> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.searchApps(query, sort, count, offset) }
	}

	suspend fun getAppInfo(packageName: String): Result<AppDetails> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.getAppInfo(packageName) }
	}

	suspend fun checkAppScreenshotExists(packageName: String, index: Int): Result<List<String>> {
		return RepositoryWrapper.wrap(
			{ listOf("${AppStoreApi.BASE_URL}app/screenshot/?пакет=${packageName}&скриншот=${index}") }
		) { AppStoreApi.instance.checkAppScreenshotExists(packageName, index) }
	}

	suspend fun getAppReviews(packageName: String, count: Int, offset: Int): Result<List<Review>> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.getAppReviews(packageName, count, offset) }
	}

	suspend fun getMyReview(packageName: String): Result<MyReview> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.getMyReview(packageName) }
	}

	suspend fun leaveReview(packageName: String, rating: Int, text: String?): Result<Unit> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.leaveReview(packageName, rating, text) }
	}

	suspend fun editReview(packageName: String, rating: Int, text: String?): Result<Unit> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.editReview(packageName, rating, text) }
	}

	suspend fun deleteReview(packageName: String): Result<Unit> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.deleteReview(packageName) }
	}

	suspend fun rateReview(reviewId: Long, rating: Int): Result<Unit> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.rateReview(reviewId, rating) }
	}

	suspend fun editReviewRating(reviewId: Long, rating: Int): Result<Unit> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.editReviewRating(reviewId, rating) }
	}

	suspend fun deleteReviewRating(reviewId: Long): Result<Unit> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.deleteReviewRating(reviewId) }
	}

	suspend fun getDeveloperInfo(developerName: String): Result<Developer> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.getDeveloperInfo(developerName) }
	}

	suspend fun getDeveloperSites(developerName: String): Result<List<Contact>> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.getDeveloperSites(developerName) }
	}

	suspend fun getDeveloperEmails(developerName: String): Result<List<Contact>> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.getDeveloperEmails(developerName) }
	}

	suspend fun getDeveloperPhones(developerName: String): Result<List<Contact>> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.getDeveloperPhones(developerName) }
	}

	suspend fun getDeveloperApps(developerName: String, sort: Int, count: Int, offset: Int): Result<List<AppPreview>> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.getDeveloperApps(developerName, sort, count, offset) }
	}

	suspend fun getDeveloperData(developerName: String): Result<DeveloperData> {
		return try {
			val developer = getDeveloperInfo(developerName).getOrElse { exception ->
				AppMetrica.reportError("Request error", "Exception", exception)
				return Result.failure(exception)
			}

			coroutineScope {
				val sites = async { getDeveloperSites(developerName) }.await()
				val emails = async { getDeveloperEmails(developerName) }.await()
				val phones = async { getDeveloperPhones(developerName) }.await()

				val developerData = DeveloperData(
					developer,
					sites.getOrDefault(emptyList()),
					emails.getOrDefault(emptyList()),
					phones.getOrDefault(emptyList())
				)

				Result.success(developerData)
			}
		} catch (e: Exception) {
			AppMetrica.reportError("Request error", "Exception", e)
			Result.failure(e)
		}
	}

	suspend fun getMyApps(): Result<List<MyApp>> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.getMyApps() }
	}

	suspend fun removeFromMyApps(packageName: String): Result<Unit> {
		return RepositoryWrapper.wrap { AppStoreApi.instance.removeFromMyApps(packageName) }
	}
}
