package ru.orlanprogressive.orlandroid.ui.common

import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BottomSheetDialogWrapper : BottomSheetDialogFragment() {

	lateinit var progressBar: ProgressBar

	private val _isLoading = MutableStateFlow(false)
	val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

	protected fun load(loader: suspend () -> Unit) {
		viewLifecycleOwner.lifecycleScope.launch {
			_isLoading.value = true
			loader()
			_isLoading.value = false
		}
	}

	protected fun observe() {
		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				isLoading.collect { state ->
					progressBar.visibility = if (state) View.VISIBLE else View.GONE
				}
			}
		}
	}
}
