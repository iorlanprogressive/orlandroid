package ru.orlanprogressive.orlandroid.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ru.orlanprogressive.orlandroid.data.repository.AccountRepository
import ru.orlanprogressive.orlandroid.databinding.DialogChangeNameBottomSheetBinding
import ru.orlanprogressive.orlandroid.ui.common.BottomSheetDialogWrapper

class ChangeNameBottomSheetBindingDialog : BottomSheetDialogWrapper() {

	private var _binding: DialogChangeNameBottomSheetBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DialogChangeNameBottomSheetBinding.inflate(inflater, container, false)
		progressBar = binding.progressBar
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupClickListener()
		observe()
	}

	private fun setupClickListener() {
		binding.apply {
			btnAccept.setOnClickListener {
				load{
					AccountRepository.changeName(name.text.toString())
						.onSuccess {
							dismiss()
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
		}
	}
}
