package ru.orlanprogressive.orlandroid.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.launch
import ru.orlanprogressive.orlandroid.R
import ru.orlanprogressive.orlandroid.data.models.Review
import ru.orlanprogressive.orlandroid.data.repository.AppRepository
import ru.orlanprogressive.orlandroid.databinding.FragmentAppDetailsBinding
import ru.orlanprogressive.orlandroid.databinding.ItemReviewBinding
import ru.orlanprogressive.orlandroid.databinding.ItemScreenshotBinding
import ru.orlanprogressive.orlandroid.network.AppStoreApi
import ru.orlanprogressive.orlandroid.ui.common.FragmentWrapper
import ru.orlanprogressive.orlandroid.ui.common.ItemsLoader

class AppDetailsFragment : FragmentWrapper() {

	private val args: AppDetailsFragmentArgs by navArgs()
	private val packageName get() = args.packageName

	private var _binding: FragmentAppDetailsBinding? = null
	private val binding get() = _binding!!

	private lateinit var screenshotsLoader: ItemsLoader<String>
	private lateinit var reviewsLoader: ItemsLoader<Review>

	private var developerName: String? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentAppDetailsBinding.inflate(inflater, container, false)
		progressBar = binding.progressBar
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		load {
			AppRepository.getAppInfo(packageName)
				.onSuccess { data ->
					binding.apply {
						developerName = data.developer

						name.text = data.name
						developer.text = developerName
						rating.text = (data.rating ?: 0.0f).toString()
						category.text = data.category
						ageRestriction.text = data.ageRestriction
						description.text = data.description
						version.text = getString(R.string.app_version_content, data.version)
						changelog.text = data.changelog
						publicationDate.text = getString(R.string.published_at_content, data.publicationDate)
						updateDate.text = getString(R.string.updated_at_content, data.updateDate)

						Glide.with(requireContext())
							.load("${AppStoreApi.BASE_URL}app/logo/?пакет=${data.packageName}")
							.placeholder(R.drawable.ic_image)
							.into(icon)

						setupScreenshotsViewer()
						setupMyReviewViewer()
						setupReviewsViewer()
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
			"Get app info",
			mapOf(
				"packageName" to packageName
			)
		)
	}

	private fun setupScreenshotsViewer() {
		@Suppress("UNCHECKED_CAST")
		screenshotsLoader = ItemsLoader(
			requireContext(),
			viewLifecycleOwner,
			binding.screenshotsRecyclerView,
			binding.screenshotsProgressBar,
			{ _, offset -> AppRepository.checkAppScreenshotExists(packageName, offset + 1) },
			ItemScreenshotBinding::inflate,
			object : DiffUtil.ItemCallback<String>() {
				override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
					return oldItem == newItem
				}

				override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
					return oldItem == newItem
				}
			},
			{ binding: ItemScreenshotBinding, item: String ->
				binding.apply {
					Glide.with(root.context)
						.load(item)
						.placeholder(R.drawable.ic_image)
						.into(root)
				}
			} as (ViewBinding, String) -> Unit,
			"скриншоты",
			1,
			resources.getInteger(R.integer.screenshots_preload_threshold),
			true
		)
	}

	private fun setupMyReviewViewer() {
		viewLifecycleOwner.lifecycleScope.launch {
			AppRepository.getMyReview(packageName)
				.onSuccess { review ->
					binding.apply {
						reviewText.clearFocus()

						reviewRating.rating = review.rating.toFloat()
						reviewText.setText(review.text)
						likesCount.text = review.likes.toString()
						dislikesCount.text = review.dislikes.toString()

						reviewRating.onRatingBarChangeListener =
							RatingBar.OnRatingBarChangeListener { _, _, fromUser ->
								if (fromUser) {
									btnSubmitReview.text = getString(R.string.action_edit_review)
									btnSubmitReview.visibility = View.VISIBLE
								}
							}
						reviewText.addTextChangedListener {
							btnSubmitReview.text = getString(R.string.action_edit_review)
							btnSubmitReview.visibility = View.VISIBLE
						}

						btnSubmitReview.visibility = View.GONE
						btnSubmitReview.setOnClickListener {
							editReview()
						}

						btnDeleteReview.visibility = View.VISIBLE
						btnDeleteReview.setOnClickListener {
							deleteReview()
						}
					}
				}
				.onFailure {
					binding.apply {
						reviewText.clearFocus()

						reviewRating.rating = 0f
						reviewText.text?.clear()

						btnSubmitReview.text = getString(R.string.action_leave_review)
						btnSubmitReview.visibility = View.VISIBLE
						btnSubmitReview.setOnClickListener {
							leaveReview()
						}

						btnDeleteReview.visibility = View.GONE
					}
				}
		}
	}

	private fun setupReviewsViewer() {
		@Suppress("UNCHECKED_CAST")
		reviewsLoader = ItemsLoader(
			requireContext(),
			viewLifecycleOwner,
			binding.reviewsRecyclerView,
			binding.reviewsProgressBar,
			{ count, offset -> AppRepository.getAppReviews(packageName, count, offset)},
			ItemReviewBinding::inflate,
			object : DiffUtil.ItemCallback<Review>() {
				override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
					return oldItem.id == newItem.id
				}

				override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
					return oldItem == newItem
				}
			},
			{ binding: ItemReviewBinding, item: Review ->
				binding.apply {
					userName.text = item.userName
					reviewRating.rating = item.rating.toFloat()
					reviewText.text = item.text
					reviewDate.text = item.date
					developerFeedbackContainer.visibility = if(item.feedback != null)
						View.VISIBLE else View.INVISIBLE
					developerFeedback.text = item.feedback
					developerFeedbackDate.text = item.feedbackDate
					likesCount.text = item.likes.toString()
					dislikesCount.text = item.dislikes.toString()

					btnLike.setImageResource(
						if(item.myRating == 1)
							R.drawable.ic_thumb_up_filled
						else
							R.drawable.ic_thumb_up_outlined
					)
					btnDislike.setImageResource(
						if(item.myRating == 0)
							R.drawable.ic_thumb_down_filled
						else
							R.drawable.ic_thumb_down_outlined
					)

					btnLike.setOnClickListener {
						handleReviewRate(
							binding,
							item.id,
							1,
							item.myRating
						)
					}
					btnDislike.setOnClickListener {
						handleReviewRate(
							binding,
							item.id,
							0,
							item.myRating
						)
					}
				}
			} as (ViewBinding, Review) -> Unit,
			"отзывы",
			resources.getInteger(R.integer.reviews_load_items_count),
			resources.getInteger(R.integer.reviews_preload_threshold),
			true
		)
	}

	private fun setupClickListeners() {
		binding.apply {
			developer.setOnClickListener {
				if (developerName != null) {
					val action =
						AppDetailsFragmentDirections.actionAppDetailsFragmentToDeveloperFragment(
							developerName!!
						)
					findNavController().navigate(action)
				}
			}
			btnLaunch.setOnClickListener {
				Toast.makeText(
					requireContext(),
					"Запуск ещё не реализован",
					Toast.LENGTH_SHORT
				).show()
			}
		}
	}

	private fun leaveReview() {
		viewLifecycleOwner.lifecycleScope.launch {
			binding.apply {
				AppRepository.leaveReview(
					packageName,
					reviewRating.rating.toInt(),
					reviewText.text.toString()
				)
					.onSuccess {
						setupMyReviewViewer()
						AppMetrica.reportEvent(
							"Leave review",
							mapOf(
								"packageName" to packageName
							)
						)
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

	private fun editReview() {
		viewLifecycleOwner.lifecycleScope.launch {
			binding.apply {
				AppRepository.editReview(
					packageName,
					reviewRating.rating.toInt(),
					reviewText.text.toString()
				)
					.onSuccess {
						setupMyReviewViewer()
						AppMetrica.reportEvent(
							"Edit review",
							mapOf(
								"packageName" to packageName
							)
						)
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

	private fun deleteReview() {
		viewLifecycleOwner.lifecycleScope.launch {
			AppRepository.deleteReview(packageName)
				.onSuccess {
					setupMyReviewViewer()
					AppMetrica.reportEvent(
						"Remove review",
						mapOf(
							"packageName" to packageName
						)
					)
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

	private fun handleReviewRate(
		binding: ItemReviewBinding,
		reviewId: Long,
		rating: Int,
		currentRating: Int?
	) {
		viewLifecycleOwner.lifecycleScope.launch {
			if (currentRating == null) {
				AppRepository.rateReview(reviewId, rating)
					.onSuccess {
						binding.apply {
							if (rating == 1)
								btnLike.setImageResource(R.drawable.ic_thumb_up_filled)
							else
								btnDislike.setImageResource(R.drawable.ic_thumb_down_filled)
						}
						reviewsLoader.reload()
					}
					.onFailure { exception ->
						Toast.makeText(
							requireContext(),
							exception.message,
							Toast.LENGTH_SHORT
						).show()
					}
			} else if (rating == currentRating) {
				AppRepository.deleteReviewRating(reviewId)
					.onSuccess {
						binding.apply {
							if (rating == 1)
								btnLike.setImageResource(R.drawable.ic_thumb_up_outlined)
							else
								btnDislike.setImageResource(R.drawable.ic_thumb_down_outlined)
						}
						reviewsLoader.reload()
					}
					.onFailure { exception ->
						Toast.makeText(
							requireContext(),
							exception.message,
							Toast.LENGTH_SHORT
						).show()
					}
			} else {
				AppRepository.editReviewRating(reviewId, rating)
					.onSuccess {
						binding.apply {
							if (rating == 1) {
								btnLike.setImageResource(R.drawable.ic_thumb_up_filled)
								btnDislike.setImageResource(R.drawable.ic_thumb_down_outlined)
							} else {
								btnLike.setImageResource(R.drawable.ic_thumb_up_outlined)
								btnDislike.setImageResource(R.drawable.ic_thumb_down_filled)
							}
						}
						reviewsLoader.reload()
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

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
