package ru.orlanprogressive.orlandroid.ui.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.orlanprogressive.orlandroid.App
import ru.orlanprogressive.orlandroid.R

open class ItemsLoader<T>(
	private val context: Context,
	private val lifecycleOwner: LifecycleOwner,
	private val recyclerView: RecyclerView,
	private val progressBar: ProgressBar,
	private var request: suspend (count: Int, offset: Int) -> Result<List<T>>,
	private val inflate: (LayoutInflater, ViewGroup, Boolean) -> ViewBinding,
	private val itemDiffCallback: DiffUtil.ItemCallback<T>,
	private val bindData: (ViewBinding, T) -> Unit,
	private var contentName: String = "контент",
	private val loadItemsCount: Int = 10,
	private val preloadThreshold: Int = 3,
	private val horizontal: Boolean = false,
	private val spanCount: Int? = null
) {

	private var itemsAdapter: ItemsAdapter

	private val _items = MutableStateFlow<List<T>>(emptyList())
	val items: StateFlow<List<T>> = _items.asStateFlow()

	private val _isLoading = MutableStateFlow(false)
	val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

	private var currentOffset = 0
	private var canLoadMore = true

	init {
		itemsAdapter = ItemsAdapter()
		setupView()
		observe()
		loadItems()
	}

	open fun setRequest(req: suspend (count: Int, offset: Int) -> Result<List<T>>) {
		request = req
		reload()
	}

	fun reload() {
		_items.value = emptyList()

		currentOffset = 0
		canLoadMore = true

		loadItems()
	}

	private fun loadItems() {
		if (_isLoading.value || !canLoadMore) return

		lifecycleOwner.lifecycleScope.launch {
			_isLoading.value = true
			request(loadItemsCount, currentOffset)
				.onSuccess { data ->
					_items.value += data
					currentOffset = _items.value.size
					canLoadMore = data.size == loadItemsCount
					ensureFillViewport()
				}
				.onFailure { exception ->
					Toast.makeText(
						context,
						exception.message ?: App.instance.getString(R.string.error_load, contentName),
						Toast.LENGTH_SHORT
					).show()
				}
			_isLoading.value = false
		}
	}

	private fun ensureFillViewport() {
		val layoutManager = recyclerView.layoutManager as LinearLayoutManager
		val totalItemCount = layoutManager.itemCount
		val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

		if (lastVisibleItemPosition >= totalItemCount - preloadThreshold)
			loadItems()
	}

	private fun setupView() {
		recyclerView.apply {
			layoutManager =
				if (spanCount == null)
					LinearLayoutManager(
						context,
						if (horizontal) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL,
						false
					)
				else
					GridLayoutManager(
						context,
						spanCount,
						if (horizontal) GridLayoutManager.HORIZONTAL else GridLayoutManager.VERTICAL,
						false
					)
			adapter = itemsAdapter
			addOnScrollListener(object : RecyclerView.OnScrollListener() {
				override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
					super.onScrolled(recyclerView, dx, dy)
					ensureFillViewport()
				}
			})
		}
	}

	private fun observe() {
		lifecycleOwner.lifecycleScope.launch {
			lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				items.collect { data ->
					itemsAdapter.submitList(data)
				}
			}
		}

		lifecycleOwner.lifecycleScope.launch {
			lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				isLoading.collect { state ->
					progressBar.visibility = if (state) View.VISIBLE else View.GONE
				}
			}
		}
	}

	private inner class ItemsAdapter : ListAdapter<T, ViewHolder>(itemDiffCallback) {

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
			val binding = inflate(LayoutInflater.from(parent.context), parent, false)
			return ViewHolder(binding)
		}

		override fun onBindViewHolder(holder: ViewHolder, position: Int) {
			holder.bind(getItem(position))
		}
	}

	private inner class ViewHolder(
		private val binding: ViewBinding
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: T) {
			bindData(binding, item)
		}
	}
}
