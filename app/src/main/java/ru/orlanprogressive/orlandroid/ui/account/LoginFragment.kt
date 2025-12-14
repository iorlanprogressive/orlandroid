package ru.orlanprogressive.orlandroid.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import ru.orlanprogressive.orlandroid.R
import ru.orlanprogressive.orlandroid.auth.AuthManager
import ru.orlanprogressive.orlandroid.data.repository.AccountRepository
import ru.orlanprogressive.orlandroid.databinding.FragmentLoginBinding
import ru.orlanprogressive.orlandroid.ui.common.FragmentWrapper

class LoginFragment : FragmentWrapper() {

	private var _binding: FragmentLoginBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentLoginBinding.inflate(inflater, container, false)
		progressBar = binding.progressBar
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		if (AuthManager.isLoggedIn()) {
			findNavController().navigateUp()
		}

		setupClickListeners()
		observe()
	}

	private fun setupClickListeners() {
		binding.apply {
			btnLogin.setOnClickListener {
				login()
			}
			btnRegister.setOnClickListener {
				val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
				findNavController().navigate(action)
			}
			btnRestorePassword.setOnClickListener {
				if (email.text.isNullOrBlank()) {
					Toast.makeText(
						requireContext(),
						getString(R.string.toast_enter_email),
						Toast.LENGTH_SHORT
					).show()
				} else {
					restorePassword()
				}
			}
			btnDoc.setOnClickListener {
				val action = LoginFragmentDirections.actionLoginFragmentToDocFragment()
				findNavController().navigate(action)
			}
		}
	}

	private fun login() {
		load {
			binding.apply {
				AccountRepository.login(
					email.text.toString(),
					password.text.toString()
				)
					.onSuccess {
						findNavController().navigateUp()
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

	private fun restorePassword() {
		load {
			val email = binding.email.text.toString()
			AccountRepository.requestPasswordRestore(email)
				.onSuccess {
					val dialog = RestorePasswordBottomSheetDialog(email) { findNavController().navigateUp() }
					dialog.show(parentFragmentManager, "RestorePasswordBottomSheet")
				}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
