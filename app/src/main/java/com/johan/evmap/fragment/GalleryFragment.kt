package com.johan.evmap.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.transition.TransitionInflater
import androidx.viewpager2.widget.ViewPager2
import com.johan.evmap.MapsActivity
import com.johan.evmap.R
import com.johan.evmap.adapter.GalleryAdapter
import com.johan.evmap.adapter.galleryTransitionName
import com.johan.evmap.api.goingelectric.ChargerPhoto
import com.johan.evmap.databinding.FragmentGalleryBinding
import com.johan.evmap.viewmodel.GalleryViewModel
import com.ortiz.touchview.TouchImageView


class GalleryFragment : Fragment(), MapsActivity.FragmentCallback {
    companion object {
        private const val EXTRA_POSITION = "position"
        private const val EXTRA_PHOTOS = "photos"
        private const val SAVED_CURRENT_PAGE_POSITION = "current_page_position"

        fun buildArgs(photos: List<ChargerPhoto>, position: Int): Bundle {
            return Bundle().apply {
                putParcelableArrayList(EXTRA_PHOTOS, ArrayList(photos))
                putInt(EXTRA_POSITION, position)
            }
        }
    }

    private lateinit var binding: FragmentGalleryBinding
    private var isReturning: Boolean = false
    private var startingPosition: Int = 0
    private var currentPosition: Int = 0
    private lateinit var galleryAdapter: GalleryAdapter
    private var currentPage: TouchImageView? = null
    private val galleryVm: GalleryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_gallery, container, false
        )
        binding.lifecycleOwner = this

        val args = requireArguments()
        startingPosition = args.getInt(EXTRA_POSITION, 0)
        currentPosition =
            savedInstanceState?.getInt(SAVED_CURRENT_PAGE_POSITION) ?: startingPosition

        galleryAdapter =
            GalleryAdapter(requireContext(), detailView = true, pageToLoad = currentPosition) {
                startPostponedEnterTransition()
            }
        binding.gallery.setPageTransformer { page, position ->
            val v = page as TouchImageView
            currentPage = v
        }
        binding.gallery.adapter = galleryAdapter
        binding.photos = args.getParcelableArrayList(EXTRA_PHOTOS)

        binding.gallery.post {
            binding.gallery.setCurrentItem(currentPosition, false)
            binding.gallery.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    currentPosition = position
                }
            })
        }

        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(R.transition.image_shared_element_transition)
        sharedElementReturnTransition = TransitionInflater.from(context)
            .inflateTransition(R.transition.image_shared_element_transition)
        setEnterSharedElementCallback(enterElementCallback)
        if (savedInstanceState == null) {
            postponeEnterTransition();
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVED_CURRENT_PAGE_POSITION, currentPosition)
    }

    private val enterElementCallback: SharedElementCallback = object : SharedElementCallback() {
        override fun onMapSharedElements(
            names: MutableList<String>,
            sharedElements: MutableMap<String, View>
        ) {
            if (isReturning) {
                val currentPage = currentPage ?: return
                val index = binding.gallery.currentItem

                if (startingPosition != currentPosition) {
                    names.clear()
                    names.add(galleryTransitionName(index))

                    sharedElements.clear()
                    sharedElements[galleryTransitionName(index)] = currentPage
                }
            }
        }
    }

    override fun getRootView(): View {
        return binding.root
    }

    override fun goBack(): Boolean {
        val image = currentPage
        if (image != null && image.currentZoom !in 0.95f..1.05f) {
            image.setZoomAnimated(1f, 0.5f, 0.5f)
            return true
        } else {
            isReturning = true
            galleryVm.galleryPosition.value = currentPosition
            return false
        }
    }

    override fun onResume() {
        super.onResume()
        val hostActivity = activity as? MapsActivity ?: return
        hostActivity.fragmentCallback = this
    }

}