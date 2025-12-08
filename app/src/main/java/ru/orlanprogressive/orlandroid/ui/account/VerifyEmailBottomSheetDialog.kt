package ru.orlanprogressive.orlandroid.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ru.orlanprogressive.orlandroid.R
import ru.orlanprogressive.orlandroid.data.repository.AccountRepository
import ru.orlanprogressive.orlandroid.databinding.DialogVerifyEmailBottomSheetBinding
import ru.orlanprogressive.orlandroid.ui.common.BottomSheetDialogWrapper

class VerifyEmailBottomSheetDialog(
	private val email: String,
	private val onSuccess: () -> Unit
) : BottomSheetDialogWrapper() {
	private var _binding: DialogVerifyEmailBottomSheetBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DialogVerifyEmailBottomSheetBinding.inflate(inflater, container, false)
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
				if (password.text.toString() != confirmPassword.text.toString()) {
					Toast.makeText(
						requireContext(),
						getString(R.string.error_passwords_not_match),
						Toast.LENGTH_SHORT
					).show()
				} else {
					load {
						AccountRepository.verifyEmail(
							code.text.toString(),
							name.text.toString(),
							email,
							password.text.toString(),
						)
							.onSuccess {
								dismiss()
								onSuccess()
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
}
