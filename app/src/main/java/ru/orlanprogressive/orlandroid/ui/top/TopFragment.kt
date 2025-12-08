package ru.orlanprogressive.orlandroid.ui.top

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.appmetrica.analytics.AppMetrica
import ru.orlanprogressive.orlandroid.R
import ru.orlanprogressive.orlandroid.data.repository.AppRepository
import ru.orlanprogressive.orlandroid.databinding.FragmentTopBinding
import ru.orlanprogressive.orlandroid.ui.common.AppsLoaderWrapper

class TopFragment : Fragment() {

	private val args: TopFragmentArgs by navArgs()
	private val category get() = args.category.takeIf { it?.isNotBlank() ?: true }

	private var _binding: FragmentTopBinding? = null
	private val binding get() = _binding!!

	private lateinit var appsLoader: AppsLoaderWrapper

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentTopBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupViewer()
		setupToolbar()
		setupSearch()
		AppMetrica.reportEvent(
			"Observe apps",
			mapOf(
				"category" to category
			)
		)
	}

	private fun setupViewer() {
		appsLoader = AppsLoaderWrapper(
			requireContext(),
			viewLifecycleOwner,
			binding.toggleSort,
			binding.recyclerView,
			binding.progressBar,
			{ sort, count, offset ->
				if (category == null)
					AppRepository.getTopApps(sort, count, offset)
				else
					AppRepository.getAppsByCategory(category!!, sort, count, offset)
			},
			{ _, item ->
				val action =
					TopFragmentDirections.actionTopFragmentToAppDetailsFragment(item.packageName)
				findNavController().navigate(action)
			},
			"список приложений",
			resources.getInteger(R.integer.apps_load_items_count),
			resources.getInteger(R.integer.apps_preload_threshold)
		)
	}

	private fun setupToolbar() {
		binding.toolbarTitle.title = if (category == null)
			getString(R.string.top_toolbar_title)
		else
			getString(R.string.categories_toolbar_title)
	}

	private fun setupSearch() {
		binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String): Boolean {
				if (query.isNotBlank()) {
					appsLoader.setRequest { sort, count, offset ->
						AppRepository.searchApps(query, sort, count, offset)
					}
					binding.toolbarTitle.title = getString(R.string.search_result_title, query)
					AppMetrica.reportEvent(
						"Observe apps",
						mapOf(
							"query" to query
						)
					)
				}
				return true
			}

			override fun onQueryTextChange(newText: String): Boolean {
				if (newText.isBlank()) {
					appsLoader.setRequest { sort, count, offset ->
						if (category == null)
							AppRepository.getTopApps(sort, count, offset)
						else
							AppRepository.getAppsByCategory(category!!, sort, count, offset)
					}
					setupToolbar()
				}
				return true
			}
		})
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
