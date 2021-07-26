package ba.grbo.weatherchecker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ba.grbo.weatherchecker.databinding.FragmentOverviewBinding
import ba.grbo.weatherchecker.ui.viewmodels.OverviewViewModel
import ba.grbo.weatherchecker.util.Constants.EMPTY_STRING
import ba.grbo.weatherchecker.util.setUp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OverviewFragment : Fragment() {
    private val viewModel: OverviewViewModel by viewModels()
    private lateinit var binding: FragmentOverviewBinding

    private val fadeIn: AlphaAnimation by lazy { AlphaAnimation(0f, 1f).setUp(resources) }
    private val fadeOut: AlphaAnimation by lazy { AlphaAnimation(1f, 0f).setUp(resources)  }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOverviewBinding.inflate(inflater, container, false)
        setListeners()
        viewModel.collectFlows()
        return binding.root
    }

    private fun setListeners() {
        setSearcherListener()
    }

    private fun setSearcherListener() {
        binding.run {
            locationSearcher.addTextChangedListener {
                it?.let { viewModel.onLocationSearcherTextChanged(it.toString()) }
            }

            locationResetter.setOnClickListener { viewModel.onLocationResetterClicked() }
        }
    }
    private fun OverviewViewModel.collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    locationResetterVisibility.collect(::onLocationResetterVisibilityChanged)
                }

                launch { resetLocationSearcherText.collect { resetLocationSearcherText() } }
            }
        }
    }

    private fun onLocationResetterVisibilityChanged(visible: Boolean?) {
        visible?.let {
            if (it) setAnimationListenerAndStartAnimation(
                binding.locationResetter,
                fadeIn,
                true
            ) else setAnimationListenerAndStartAnimation(
                binding.locationResetter,
                fadeOut,
                false
            )
        }
    }

    private fun resetLocationSearcherText() {
        binding.locationSearcher.setText(EMPTY_STRING)
    }

    private fun setAnimationListenerAndStartAnimation(
        view: View,
        animation: AlphaAnimation,
        fadingIn: Boolean
    ) {
        animation.setAnimationListener(
            getAnimationListener(
                view,
                fadingIn,
                fadeIn,
                fadeOut
            )
        )
        view.startAnimation(animation)
    }

    private fun getAnimationListener(
        view: View,
        fadingIn: Boolean,
        fadeIn: AlphaAnimation,
        fadeOut: AlphaAnimation
    ) = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            if (!fadingIn && view is ImageButton) view.isEnabled = false
            else if (view is ImageButton) view.isEnabled = true
        }

        override fun onAnimationEnd(animation: Animation?) {
            if (fadingIn) {
                view.visibility = View.VISIBLE
                fadeIn.setAnimationListener(null)
            } else {
                view.visibility = View.INVISIBLE
                fadeOut.setAnimationListener(null)
            }
        }

        override fun onAnimationRepeat(animation: Animation?) {
        }
    }
}