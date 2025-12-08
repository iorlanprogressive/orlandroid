package ru.orlanprogressive.orlandroid.ui.myapps

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.launch
import ru.orlanprogressive.orlandroid.R
import ru.orlanprogressive.orlandroid.auth.AuthManager
import ru.orlanprogressive.orlandroid.data.models.MyApp
import ru.orlanprogressive.orlandroid.data.repository.AppRepository
import ru.orlanprogressive.orlandroid.databinding.FragmentMyAppsBinding
import ru.orlanprogressive.orlandroid.databinding.ItemMyAppBinding
import ru.orlanprogressive.orlandroid.network.AppStoreApi
import ru.orlanprogressive.orlandroid.ui.common.ItemsLoader

class MyAppsFragment : Fragment() {

	private var _binding: FragmentMyAppsBinding? = null
	private val binding get() = _binding!!

	private lateinit var appsLoader: ItemsLoader<MyApp>

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentMyAppsBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		if (!AuthManager.isLoggedIn()) {
			val action = MyAppsFragmentDirections.actionMyAppsFragmentToLoginFragment()
			findNavController().navigate(action)
		}

		setupViewer()

		AppMetrica.reportEvent("Observe my apps")
	}

	private fun setupViewer() {
		@Suppress("UNCHECKED_CAST")
		appsLoader = ItemsLoader(
			requireContext(),
			viewLifecycleOwner,
			binding.recyclerView,
			binding.progressBar,
			{ _, _ -> AppRepository.getMyApps() },
			ItemMyAppBinding::inflate,
			object  : DiffUtil.ItemCallback<MyApp>() {
				override fun areItemsTheSame(oldItem: MyApp, newItem: MyApp): Boolean {
					return oldItem.packageName == newItem.packageName
				}

				override fun areContentsTheSame(oldItem: MyApp, newItem: MyApp): Boolean {
					return oldItem == newItem
				}
			},
			{ binding: ItemMyAppBinding, item: MyApp ->
				binding.apply {
					name.text = item.name

					Glide.with(root.context)
						.load("${AppStoreApi.BASE_URL}app/logo/?пакет=${item.packageName}")
						.placeholder(R.drawable.ic_image)
						.into(icon)

					root.setOnClickListener {
						Toast.makeText(
							requireContext(),
							getString(R.string.toast_launch_not_implemented),
							Toast.LENGTH_SHORT
						).show()
					}

					root.setOnLongClickListener {
						showContextMenu(binding, item)
						true
					}
				}
			} as (ViewBinding, MyApp) -> Unit,
			"список приложений",
			spanCount = resources.getInteger(R.integer.my_apps_grid_span_count)
		)
	}

	private fun showContextMenu(binding: ItemMyAppBinding, app: MyApp) {
		val popupMenu = PopupMenu(requireContext(), binding.root)
		popupMenu.menu.apply {
			add(getString(R.string.context_action_about))
				.setOnMenuItemClickListener {
					val action = MyAppsFragmentDirections.actionMyAppsFragmentToAppDetailsFragment(app.packageName)
					findNavController().navigate(action)
					true
				}
			add(getString(R.string.context_action_share))
				.setOnMenuItemClickListener {
					shareApp(app)
					true
				}
			add(getString(R.string.context_action_remove))
				.setOnMenuItemClickListener {
					removeApp(app.packageName)
					true
				}
		}
		popupMenu.show()
	}

	private fun shareApp(app: MyApp) {
		val shareIntent = Intent(Intent.ACTION_SEND).apply {
			type = "text/plain"

			putExtra(Intent.EXTRA_SUBJECT, app.name)
			putExtra(
				Intent.EXTRA_TEXT,
				getString(
					R.string.share_app_text,
					app.name,
					getString(R.string.app_name),
					"https://orlan-progressive.ru/app/${app.packageName}"
				)
			)
		}
		AppMetrica.reportEvent(
			"Share app",
			mapOf(
				"packageName" to app.packageName
			)
		)
		startActivity(Intent.createChooser(
			shareIntent,
			getString(R.string.share_app_title)
		))
	}

	private fun removeApp(packageName: String) {
		viewLifecycleOwner.lifecycleScope.launch {
			AppRepository.removeFromMyApps(packageName)
				.onSuccess {
					appsLoader.reload()
					AppMetrica.reportEvent(
						"Remove app",
						mapOf(
							"packageName" to packageName
						)
					)
				}
				.onFailure { exception ->
					Toast.makeText(
						requireContext(),
						exception.message,
						Toast.LENGTH_SHORT
					).show()
				}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
