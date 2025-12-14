package ru.orlanprogressive.orlandroid.ui.developer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import ru.orlanprogressive.orlandroid.R
import ru.orlanprogressive.orlandroid.data.models.Contact
import ru.orlanprogressive.orlandroid.data.models.DeveloperData
import ru.orlanprogressive.orlandroid.data.repository.AppRepository
import ru.orlanprogressive.orlandroid.databinding.FragmentDeveloperBinding
import ru.orlanprogressive.orlandroid.network.AppStoreApi
import ru.orlanprogressive.orlandroid.ui.common.AppsLoaderWrapper
import kotlin.collections.first
import androidx.core.net.toUri
import io.appmetrica.analytics.AppMetrica
import ru.orlanprogressive.orlandroid.ui.common.FragmentWrapper

class DeveloperFragment : FragmentWrapper() {

	private val args: DeveloperFragmentArgs by navArgs()
	private val developerName get() = args.developer

	private var _binding: FragmentDeveloperBinding? = null
	private val binding get() = _binding!!

	private lateinit var developerData: DeveloperData
	private lateinit var appsLoader: AppsLoaderWrapper

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentDeveloperBinding.inflate(inflater, container, false)
		progressBar = binding.progressBar
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		load {
			AppRepository.getDeveloperData(developerName)
				.onSuccess { data ->
					developerData = data
					binding.apply {
						name.text = developerName
						description.text = developerData.developer.description
						status.text = developerData.developer.status

						Glide.with(requireContext())
							.load("${AppStoreApi.BASE_URL}developer/logo/?разработчик=${developerName}")
							.into(binding.logo)

						btnWebsite.isEnabled = developerData.websites.isNotEmpty()
						btnEmail.isEnabled = developerData.emails.isNotEmpty()
						btnPhone.isEnabled = developerData.phones.isNotEmpty()

						setupViewer()
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

		AppMetrica.reportEvent(
			"Get developer info",
			mapOf(
				"developerName" to developerName
			)
		)
	}

	private fun setupViewer() {
		appsLoader = AppsLoaderWrapper(
			requireContext(),
			viewLifecycleOwner,
			binding.toggleSort,
			binding.recyclerView,
			binding.progressBar,
			{ sort, count, offset ->
				AppRepository.getDeveloperApps(developerName, sort, count, offset)
			},
			{ _, item ->
				val action =
					DeveloperFragmentDirections.actionDeveloperFragmentToAppDetailsFragment(item.packageName)
				findNavController().navigate(action)
			},
			"список приложений",
			resources.getInteger(R.integer.apps_load_items_count),
			resources.getInteger(R.integer.apps_preload_threshold)
		)
	}

	private fun setupClickListeners() {
		binding.btnWebsite.setOnClickListener {
			openContactOptions(developerData.websites, ContactType.WEBSITE)
		}
		binding.btnEmail.setOnClickListener {
			openContactOptions(developerData.emails, ContactType.EMAIL)
		}
		binding.btnPhone.setOnClickListener {
			openContactOptions(developerData.phones, ContactType.PHONE)
		}
	}

	private fun openContactOptions(contacts: List<Contact>, type: ContactType) {
		if (contacts.size == 1)
			handleContactAction(contacts.first(), type)
		else
			showContactBottomSheet(contacts, type)
	}

	private fun showContactBottomSheet(contacts: List<Contact>, type: ContactType) {
		val dialog = ContactBottomSheetDialog(contacts, type) { contact ->
			handleContactAction(contact, type)
		}
		dialog.show(parentFragmentManager, "ContactBottomSheet")
	}

	private fun handleContactAction(contact: Contact, type: ContactType) {
		AppMetrica.reportEvent(
			"Open developer contact option",
			mapOf(
				"contactLabel" to contact.label,
				"contactLink" to contact.link
			)
		)
		when (type) {
			ContactType.WEBSITE -> openWebsite(contact.link)
			ContactType.EMAIL -> sendEmail(contact.link)
			ContactType.PHONE -> callPhone(contact.link)
		}
	}

	private fun openWebsite(url: String) {
		try {
			val websiteUrl = if (url.startsWith("http")) url else "https://$url"
			val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
			startActivity(intent)
		} catch (_: Exception) {
			Toast.makeText(
				requireContext(),
				getString(R.string.error_link_website),
				Toast.LENGTH_SHORT
			).show()
		}
	}

	private fun sendEmail(email: String) {
		try {
			val intent = Intent(Intent.ACTION_SENDTO, "mailto:$email".toUri())
			startActivity(intent)
		} catch (_: Exception) {
			Toast.makeText(
				requireContext(),
				getString(R.string.error_link_email),
				Toast.LENGTH_SHORT
			).show()
		}
	}

	private fun callPhone(phone: String) {
		try {
			val intent = Intent(Intent.ACTION_DIAL, "tel:$phone".toUri())
			startActivity(intent)
		} catch (_: Exception) {
			Toast.makeText(
				requireContext(),
				getString(R.string.error_link_phone),
				Toast.LENGTH_SHORT
			).show()
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
