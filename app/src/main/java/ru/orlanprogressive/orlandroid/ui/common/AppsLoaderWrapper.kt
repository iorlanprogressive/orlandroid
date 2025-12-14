package ru.orlanprogressive.orlandroid.ui.common

import android.content.Context
import android.widget.ProgressBar
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.orlanprogressive.orlandroid.R
import ru.orlanprogressive.orlandroid.data.models.AppPreview
import ru.orlanprogressive.orlandroid.databinding.ItemAppBinding
import ru.orlanprogressive.orlandroid.network.AppStoreApi

@Suppress("UNCHECKED_CAST")
class AppsLoaderWrapper(
	context: Context,
	lifecycleOwner: LifecycleOwner,
	toggleSort: MaterialButtonToggleGroup,
	recyclerView: RecyclerView,
	progressBar: ProgressBar,
	private val request: suspend (sort: Int, count: Int, offset: Int) -> Result<List<AppPreview>>,
	onClick: (ItemAppBinding, AppPreview) -> Unit,
	contentName: String = "список приложений",
	loadItemsCount: Int = 10,
	preloadThreshold: Int = 3,
	horizontal: Boolean = false,
	private val _sort: MutableStateFlow<Int> = MutableStateFlow(1),
	val sort: StateFlow<Int> = _sort.asStateFlow()
) : ItemsLoader<AppPreview>(
	context,
	lifecycleOwner,
	recyclerView,
	progressBar,
	{ count, offset -> request(sort.value, count, offset) },
	ItemAppBinding::inflate,
	object : DiffUtil.ItemCallback<AppPreview>() {
		override fun areItemsTheSame(oldItem: AppPreview, newItem: AppPreview): Boolean {
			return oldItem.packageName == newItem.packageName
		}

		override fun areContentsTheSame(oldItem: AppPreview, newItem: AppPreview): Boolean {
			return oldItem == newItem
		}
	},
	{ binding: ItemAppBinding, item: AppPreview ->
		binding.apply {
			name.text = item.name
			developer.text = item.developer
			category.text = item.category
			ageRestriction.text = item.ageRestriction
			rating.text = (item.rating ?: 0.0f).toString()

			Glide.with(root.context)
				.load("${AppStoreApi.BASE_URL}app/logo/?пакет=${item.packageName}")
				.placeholder(R.drawable.ic_image)
				.into(icon)

			root.setOnClickListener {
				onClick(binding, item)
			}
		}
	} as (ViewBinding, AppPreview) -> Unit,
	contentName,
	loadItemsCount,
	preloadThreshold,
	horizontal
) {

	init {
		toggleSort.addOnButtonCheckedListener { _, checkedId, isChecked ->
			if (isChecked) {
				when (checkedId) {
					R.id.btn_rating -> _sort.value = 1
					R.id.btn_users -> _sort.value = 0
				}
				this.reload()
			}
		}
	}

	fun setRequest(req: suspend (sort: Int, count: Int, offset: Int) -> Result<List<AppPreview>>) {
		this.setRequest { count, offset -> req(sort.value, count, offset) }
	}
}
