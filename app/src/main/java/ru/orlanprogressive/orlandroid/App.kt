package ru.orlanprogressive.orlandroid

import android.app.Application
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

class App : Application() {

	companion object {

		private const val APPMETRICA_API_KEY = "4615e0ec-6881-4813-a847-d26efa25e747"
		private const val APPMETRICA_SESSION_TIMEOUT = 10
		private const val APPMETRICA_ANR_MONITORING_TIMEOUT = 5

		lateinit var instance: App
			private set
	}

	override fun onCreate() {
		super.onCreate()
		instance = this
		setupAppMetrica()
	}

	private fun setupAppMetrica() {
		val config = AppMetricaConfig.newConfigBuilder(APPMETRICA_API_KEY)
			.withAppOpenTrackingEnabled(true)
			.withSessionsAutoTrackingEnabled(true)
			.withSessionTimeout(APPMETRICA_SESSION_TIMEOUT)
			.withAnrMonitoring(true)
			.withAnrMonitoringTimeout(APPMETRICA_ANR_MONITORING_TIMEOUT)
			.withCrashReporting(true)
			.withNativeCrashReporting(true)
			.withDataSendingEnabled(true)
			.withLocationTracking(false)
			.withLogs()
			.build()

		AppMetrica.activate(applicationContext, config)
		AppMetrica.enableActivityAutoTracking(this)
	}
}
