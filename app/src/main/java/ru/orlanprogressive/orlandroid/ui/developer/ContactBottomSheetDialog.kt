package ru.orlanprogressive.orlandroid.ui.developer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.orlanprogressive.orlandroid.data.models.Contact
import ru.orlanprogressive.orlandroid.databinding.DialogContactBottomSheetBinding
import ru.orlanprogressive.orlandroid.databinding.ItemContactBinding
import ru.orlanprogressive.orlandroid.ui.common.ItemsLoader

class ContactBottomSheetDialog(
	private val contacts: List<Contact>,
	private val type: ContactType,
	private val onClick: (Contact) -> Unit
) : BottomSheetDialogFragment() {

	private var _binding: DialogContactBottomSheetBinding? = null
	private val binding get() = _binding!!

	private lateinit var contactsLoader: ItemsLoader<Contact>

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DialogContactBottomSheetBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		@Suppress("UNCHECKED_CAST")
		contactsLoader = ItemsLoader(
			requireContext(),
			viewLifecycleOwner,
			binding.recyclerView,
			binding.progressBar,
			{ _, _ -> Result.success(contacts)},
			ItemContactBinding::inflate,
			object : DiffUtil.ItemCallback<Contact>() {
				override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
					return oldItem.link == newItem.link
				}

				override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
					return oldItem == newItem
				}
			},
			{ binding: ItemContactBinding, item: Contact ->
				binding.apply {
					label.text = item.label
					link.text = item.link

					root.setOnClickListener {
						onClick(item)
					}
				}
			} as (ViewBinding, Contact) -> Unit,
			when(type) {
				ContactType.WEBSITE -> "сайты"
				ContactType.EMAIL -> "почты"
				ContactType.PHONE -> "телефоны"
			}
		)
	}
}
