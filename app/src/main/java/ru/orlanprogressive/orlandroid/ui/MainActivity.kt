package ru.orlanprogressive.orlandroid.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import ru.orlanprogressive.orlandroid.R
import ru.orlanprogressive.orlandroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding

	private lateinit var navController: NavController

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		navController = (supportFragmentManager.findFragmentById(R.id.host_container) as NavHostFragment).navController

		binding.bottomNavigation.setupWithNavController(navController)

		handleDeepLink()
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		setIntent(intent)
		handleDeepLink()
	}

	private fun handleDeepLink() {
		navController.handleDeepLink(intent)
	}
}
