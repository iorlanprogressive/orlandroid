package ru.orlanprogressive.orlandroid.ui.doc

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import ru.orlanprogressive.orlandroid.databinding.FragmentDocBinding
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.destination.ImageDestinationProcessor
import io.noties.markwon.image.gif.GifMediaDecoder
import io.noties.markwon.image.svg.SvgMediaDecoder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import ru.orlanprogressive.orlandroid.R

class DocFragment : Fragment() {

	private companion object {

		private const val BASE_RAW_URL = "https://iorlanprogressive.github.io/doc-orlandroid/"
	}

	private val args: DocFragmentArgs by navArgs()
	private val link get() = args.link.takeIf { it?.isNotBlank() ?: true } ?: BASE_RAW_URL

	private var _binding: FragmentDocBinding? = null
	private val binding get() = _binding!!

	private lateinit var client: OkHttpClient
	private lateinit var markwon: Markwon

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentDocBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupMarkdown()
		loadMarkdown(link)
	}

	private fun setupMarkdown() {
		client = OkHttpClient()
		markwon = Markwon.builder(requireContext())
			.usePlugin(LinkifyPlugin.create())
			.usePlugin(StrikethroughPlugin.create())
			.usePlugin(TaskListPlugin.create(requireContext()))
			.usePlugin(ImagesPlugin.create { plugin ->
				plugin.addMediaDecoder(SvgMediaDecoder.create())
				plugin.addMediaDecoder(GifMediaDecoder.create())
			})
			.usePlugin(object : AbstractMarkwonPlugin() {
				override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
					builder.imageDestinationProcessor(object : ImageDestinationProcessor() {
						override fun process(destination: String) = getLink(destination)
					})
				}
			})
			.usePlugin(object : AbstractMarkwonPlugin() {
				override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
					builder.linkResolver { _, link ->
						if (getLinkType(link) == LinkType.EXTERNAL) {
							val intent = Intent(Intent.ACTION_VIEW, link.toUri())
							startActivity(intent)
						} else {
							val action = DocFragmentDirections.actionDocFragmentSelf(getLink(link))
							findNavController().navigate(action)
						}
					}
				}
			})
			.build()
	}

	private fun loadMarkdown(link: String) {
		client.newCall(
			Request.Builder().url("${ensureLinkHasSlash(link)}README.md").get().build()
		).enqueue(object : Callback {
			override fun onFailure(call: Call, e: IOException) {
				activity?.runOnUiThread {
					Toast.makeText(
						requireContext(),
						e.message,
						Toast.LENGTH_SHORT
					).show()
				}
			}

			override fun onResponse(call: Call, response: Response) {
				if (response.isSuccessful)
					activity?.runOnUiThread {
						response.body.string().let { content ->
							markwon.setMarkdown(binding.markdown, content)
						}
					}
				else
					activity?.runOnUiThread {
						Toast.makeText(
							requireContext(),
							getString(
								R.string.error_request_failure_status_code,
								response.code
							),
							Toast.LENGTH_SHORT
						).show()
					}
			}
		})
	}

	private fun getLink(link: String): String {
		return when (getLinkType(link)) {
			LinkType.EXTERNAL -> link
			LinkType.INTERNAL_ABSOLUTE -> "${ensureLinkHasNoSlash(BASE_RAW_URL)}$link"
			LinkType.INTERNAL_RELATIVE -> "${ensureLinkHasSlash(this.link)}$link"
		}
	}

	private fun getLinkType(link: String) = when {
		link.startsWith("http://") || link.startsWith("https://") -> LinkType.EXTERNAL
		link.startsWith('/') -> LinkType.INTERNAL_ABSOLUTE
		else -> LinkType.INTERNAL_RELATIVE
	}

	private fun ensureLinkHasSlash(link: String) = "${ensureLinkHasNoSlash(link)}/"

	private fun ensureLinkHasNoSlash(link: String) = link.removeSuffix("/")

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
