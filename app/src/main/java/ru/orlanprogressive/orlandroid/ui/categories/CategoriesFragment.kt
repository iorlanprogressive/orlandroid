package ru.orlanprogressive.orlandroid.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import ru.orlanprogressive.orlandroid.R
import ru.orlanprogressive.orlandroid.databinding.FragmentCategoriesBinding
import ru.orlanprogressive.orlandroid.databinding.ItemCategoryBinding
import ru.orlanprogressive.orlandroid.ui.common.ItemsLoader

class CategoriesFragment : Fragment() {

	private var _binding: FragmentCategoriesBinding? = null
	private val binding get() = _binding!!

	private lateinit var categories: List<String>
	private lateinit var categoriesLoader: ItemsLoader<String>

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentCategoriesBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		categories = resources.getStringArray(R.array.categories).toList()

		@Suppress("UNCHECKED_CAST")
		categoriesLoader = ItemsLoader(
			requireContext(),
			viewLifecycleOwner,
			binding.recyclerView,
			binding.progressBar,
			{ _, _ -> Result.success(categories) },
			ItemCategoryBinding::inflate,
			object : DiffUtil.ItemCallback<String>() {
				override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
					return oldItem == newItem
				}

				override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
					return oldItem == newItem
				}
			},
			{ binding: ItemCategoryBinding, item: String ->
				binding.apply {
					category.text = item
					root.setOnClickListener {
						val action = CategoriesFragmentDirections.actionCategoriesFragmentToTopFragment(item)
						findNavController().navigate(action)
					}
				}
			} as (ViewBinding, String) -> Unit,
			"список категорий",
			spanCount = resources.getInteger(R.integer.categories_grid_span_count)
		)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
