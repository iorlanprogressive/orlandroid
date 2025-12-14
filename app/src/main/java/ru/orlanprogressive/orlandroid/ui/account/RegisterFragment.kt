package ru.orlanprogressive.orlandroid.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import ru.orlanprogressive.orlandroid.auth.AuthManager
import ru.orlanprogressive.orlandroid.data.repository.AccountRepository
import ru.orlanprogressive.orlandroid.databinding.FragmentRegisterBinding
import ru.orlanprogressive.orlandroid.ui.common.FragmentWrapper

class RegisterFragment : FragmentWrapper() {

	private var _binding: FragmentRegisterBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentRegisterBinding.inflate(inflater, container, false)
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
			btnRegister.setOnClickListener {
				register()
			}
			btnLogin.setOnClickListener {
				findNavController().navigateUp()
			}
		}
	}

	private fun register() {
		load {
			val email = binding.email.text.toString()
			AccountRepository.register(email)
				.onSuccess {
					val dialog = VerifyEmailBottomSheetDialog(email) { findNavController().navigateUp() }
					dialog.show(parentFragmentManager, "VerifyEmailBottomSheet")
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
