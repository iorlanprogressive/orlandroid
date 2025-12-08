package ru.orlanprogressive.orlandroid.auth

import android.content.Context
import android.util.Base64
import ru.orlanprogressive.orlandroid.App
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import androidx.core.content.edit
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.profile.Attribute
import io.appmetrica.analytics.profile.UserProfile
import ru.orlanprogressive.orlandroid.network.AppStoreApi

object AuthManager {

	private const val PREFS_NAME = "auth_prefs"
	private const val KEY_ACCESS_TOKEN = "access_token"
	private const val KEY_ISSUE_TOKEN = "issue_token"
	private const val KEY_USER_EMAIL = "user_email"

	private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
	private const val KEY_ALIAS = "auth_key"
	private const val ALGORITHM = "AES"
	private const val KEY_SIZE = 256
	private const val TRANSFORMATION = "AES/GCM/NoPadding"
	private const val IV_LENGTH = 12
	private const val TAG_LENGTH = 128

	private val sharedPrefs by lazy {
		App.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
	}

	private val keyStore by lazy {
		KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
	}

	fun login(accessToken: String, issueToken: String, email: String) {
		sharedPrefs.edit {
			putString(KEY_ACCESS_TOKEN, encrypt(accessToken))
			putString(KEY_ISSUE_TOKEN, encrypt(issueToken))
			putString(KEY_USER_EMAIL, encrypt(email))
		}
		AppStoreApi.setCredentials()
		AppMetrica.reportUserProfile(
			UserProfile.newBuilder()
				.apply(Attribute.emailHash().withEmailValues(email))
				.build()
		)
	}

	fun refreshTokens(accessToken: String, issueToken: String) {
		sharedPrefs.edit {
			putString(KEY_ACCESS_TOKEN, encrypt(accessToken))
			putString(KEY_ISSUE_TOKEN, encrypt(issueToken))
		}
		AppStoreApi.setCredentials()
	}

	fun editEmail(email: String) {
		sharedPrefs.edit {
			putString(KEY_USER_EMAIL, encrypt(email))
		}
		AppStoreApi.setCredentials()
		AppMetrica.reportUserProfile(
			UserProfile.newBuilder()
				.apply(Attribute.emailHash().withEmailValues(email))
				.build()
		)
	}

	fun getAccessToken(): String? {
		return sharedPrefs.getString(KEY_ACCESS_TOKEN, null)?.let { decrypt(it) }
	}

	fun getIssueToken(): String? {
		return sharedPrefs.getString(KEY_ISSUE_TOKEN, null)?.let { decrypt(it) }
	}

	fun getEmail(): String? {
		return sharedPrefs.getString(KEY_USER_EMAIL, null)?.let { decrypt(it) }
	}

	fun isLoggedIn(): Boolean {
		return getEmail() != null && getIssueToken() != null
	}

	fun logout() {
		sharedPrefs.edit {
			remove(KEY_ACCESS_TOKEN)
			remove(KEY_ISSUE_TOKEN)
			remove(KEY_USER_EMAIL)
		}
		AppStoreApi.setCredentials()
	}

	private fun getOrCreateSecretKey(): SecretKey {
		if (keyStore.containsAlias(KEY_ALIAS)) {
			return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
		} else {
			return KeyGenerator.getInstance(ALGORITHM, KEYSTORE_PROVIDER).run {
				init(
					android.security.keystore.KeyGenParameterSpec.Builder(
						KEY_ALIAS,
						android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
					)
						.setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
						.setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
						.setKeySize(KEY_SIZE)
						.build()
				)
				generateKey()
			}
		}
	}

	private fun encrypt(data: String): String {
		val cipher = Cipher.getInstance(TRANSFORMATION)

		cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

		return Base64.encodeToString(
			cipher.iv + cipher.doFinal(data.toByteArray(Charsets.UTF_8)),
			Base64.DEFAULT
		)
	}

	private fun decrypt(encryptedData: String): String? {
		return try {
			val cipher = Cipher.getInstance(TRANSFORMATION)

			val combined = Base64.decode(encryptedData, Base64.DEFAULT)

			cipher.init(
				Cipher.DECRYPT_MODE,
				getOrCreateSecretKey(),
				GCMParameterSpec(TAG_LENGTH, combined.copyOfRange(0, IV_LENGTH))
			)

			String(
				cipher.doFinal(combined.copyOfRange(IV_LENGTH, combined.size)),
				Charsets.UTF_8
			)
		} catch (e: Exception) {
			AppMetrica.reportError("Shared prefs decrypt error", e)
			null
		}
	}
}
