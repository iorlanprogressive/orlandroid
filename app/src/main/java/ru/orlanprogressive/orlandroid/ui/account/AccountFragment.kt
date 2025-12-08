package ru.orlanprogressive.orlandroid.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.launch
import ru.orlanprogressive.orlandroid.auth.AuthManager
import ru.orlanprogressive.orlandroid.data.repository.AccountRepository
import ru.orlanprogressive.orlandroid.databinding.FragmentAccountBinding
import ru.orlanprogressive.orlandroid.ui.common.FragmentWrapper

class AccountFragment : FragmentWrapper() {

	private var _binding: FragmentAccountBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentAccountBinding.inflate(inflater, container, false)
		progressBar = binding.progressBar
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		if (!AuthManager.isLoggedIn()) {
			requireLogin()
			return
		}

		load {
			AccountRepository.getAccountName()
				.onSuccess { userName ->
					binding.apply {
						name.text = userName
						email.text = AuthManager.getEmail()
					}
				}
				.onFailure { exception ->
					Toast.makeText(
						requireContext(),
						exception.message,
						Toast.LENGTH_SHORT
					).show()
				}
		}

		setupClickListeners()
		observe()

		AppMetrica.reportEvent("Get account info")
	}

	private fun setupClickListeners() {
		binding.apply {
			btnLogout.setOnClickListener {
				logout()
			}
			btnChangePassword.setOnClickListener {
				changePassword()
			}
			name.setOnClickListener {
				changeName()
			}
			email.setOnClickListener {
				changeEmail()
			}
			btnDoc.setOnClickListener {
				val action = AccountFragmentDirections.actionAccountFragmentToDocFragment()
				findNavController().navigate(action)
			}
		}
	}

	private fun logout() {
		viewLifecycleOwner.lifecycleScope.launch {
			AccountRepository.logout()
				.onSuccess {
					requireLogin()
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

	private fun changePassword() {
		val dialog = ChangePasswordBottomSheetDialog()
		dialog.show(parentFragmentManager, "ChangePasswordBottomSheet")
	}

	private fun changeName() {
		val dialog = ChangeNameBottomSheetBindingDialog()
		dialog.show(parentFragmentManager, "ChangeNameBottomSheet")
	}

	private fun changeEmail() {
		val dialog = ChangeEmailBottomSheetDialog()
		dialog.show(parentFragmentManager, "ChangeEmailBottomSheet")
	}

	private fun requireLogin() {
		val action = AccountFragmentDirections.actionAccountFragmentToLoginFragment()
		findNavController().navigate(action)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
