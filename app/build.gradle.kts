import java.io.FileInputStream
import java.util.Properties

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.androidx.navigation.safeargs)
}

android {
	namespace = "ru.orlanprogressive.orlandroid"

	defaultConfig {
		applicationId = "ru.orlanprogressive.orlandroid"
		minSdk = 26
		targetSdk = 36
		versionCode = 1
		versionName = "1.0.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	signingConfigs {
		create("release") {
			val keystorePropertiesFile = rootProject.file("release-signing.properties")
			val keystoreProperties = Properties()
			keystoreProperties.load(FileInputStream(keystorePropertiesFile))

			storeFile = file(keystoreProperties.getProperty("storeFile"))
			storePassword = keystoreProperties.getProperty("storePassword")
			keyAlias = keystoreProperties.getProperty("keyAlias")
			keyPassword = keystoreProperties.getProperty("keyPassword")
		}
	}

	compileSdk {
		version = release(36)
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
			signingConfig = signingConfigs.getByName("release")
		}
	}

	buildFeatures {
		viewBinding = true
		dataBinding = true
	}

	buildToolsVersion = "36.0.0"
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)
	implementation(libs.androidx.activity)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.navigation.fragment.ktx)
	implementation(libs.androidx.navigation.ui.ktx)
	implementation(libs.androidx.lifecycle.common.jvm)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.recyclerview)
	implementation(libs.androidx.swiperefreshlayout)
	implementation(libs.androidx.security.crypto)
	implementation(libs.squareup.retrofit)
	implementation(libs.squareup.retrofit.converter.moshi)
	implementation(libs.squareup.moshi)
	implementation(libs.squareup.moshi.kotlin)
	implementation(libs.squareup.okhttp)
	implementation(libs.squareup.okhttp.logging.interceptor)
	implementation(libs.bumptech.glide)
	implementation(libs.caverock.androidsvg)
	implementation(libs.droidsonroids.androidgif)
	implementation(libs.noties.markdown.core)
	implementation(libs.noties.markdown.linkify)
	implementation(libs.noties.markdown.strikethrough)
	implementation(libs.noties.markdown.tasklist)
	implementation(libs.noties.markdown.image)
	implementation(libs.appmetrica)

	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}
