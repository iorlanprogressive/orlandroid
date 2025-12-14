package ru.orlanprogressive.orlandroid.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ru.orlanprogressive.orlandroid.data.repository.AccountRepository
import ru.orlanprogressive.orlandroid.databinding.DialogChangeEmailBottomSheetBinding
import ru.orlanprogressive.orlandroid.ui.common.BottomSheetDialogWrapper

class ChangeEmailBottomSheetDialog : BottomSheetDialogWrapper() {

	private var _binding: DialogChangeEmailBottomSheetBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DialogChangeEmailBottomSheetBinding.inflate(inflater, container, false)
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
			verifyCodeInputContainer.visibility = View.GONE
			emailTextInputContainer.visibility = View.VISIBLE

			btnAccept.visibility = View.GONE
			btnRequireVerifyCode.visibility = View.VISIBLE

			btnRequireVerifyCode.setOnClickListener {
				load {
					AccountRepository.requestEmailChange(email.text.toString())
						.onSuccess {
							emailTextInputContainer.visibility = View.GONE
							verifyCodeInputContainer.visibility = View.VISIBLE

							btnRequireVerifyCode.visibility = View.GONE
							btnAccept.visibility = View.VISIBLE
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

			btnAccept.setOnClickListener {
				load {
					AccountRepository.changeEmail(code.text.toString())
						.onSuccess { _ ->
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
