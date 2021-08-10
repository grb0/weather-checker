package ba.grbo.weatherchecker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.weatherchecker.data.models.local.Place
import ba.grbo.weatherchecker.databinding.SuggestedPlaceBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SuggestedPlaceAdapter(
    private val onSuggestedPlacesChanged: () -> Unit,
    private val onClick: (Place) -> Unit,
    var rippleColor: MutableStateFlow<Int>,
    private val viewLifecyclerOwner: LifecycleOwner
) : ListAdapter<Place, SuggestedPlaceAdapter.SuggestedPlaceHolder>(PlaceDiffCallbacks()) {
    var hasInternet = MutableStateFlow(false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedPlaceHolder {
        return SuggestedPlaceHolder.from(parent, viewLifecyclerOwner)
    }

    override fun onBindViewHolder(holder: SuggestedPlaceHolder, position: Int) {
        holder.bind(getItem(position), onClick, rippleColor, hasInternet)
    }

    class SuggestedPlaceHolder private constructor(
        private val binding: SuggestedPlaceBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(
                parent: ViewGroup,
                viewLifecyclerOwner: LifecycleOwner
            ) = SuggestedPlaceHolder(
                SuggestedPlaceBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ).apply { lifecycleOwner = viewLifecyclerOwner }
            )
        }

        fun bind(
            place: Place,
            onClick: (Place) -> Unit,
            rippleColor: StateFlow<Int>,
            hasInternet: StateFlow<Boolean>
        ) {
            binding.place = place
            binding.hasInternet = hasInternet
            binding.suggestedPlaceRippleLayout.setRippleColor(rippleColor.value)
            binding.root.setOnClickListener { onClick(place) }
            binding.executePendingBindings()
        }
    }

    override fun onCurrentListChanged(
        previousList: MutableList<Place>,
        currentList: MutableList<Place>
    ) {
        onSuggestedPlacesChanged()
    }
}

class PlaceDiffCallbacks : DiffUtil.ItemCallback<Place>() {
    override fun areItemsTheSame(
        oldItem: Place,
        newItem: Place
    ) = oldItem.coordinate == newItem.coordinate

    override fun areContentsTheSame(oldItem: Place, newItem: Place) = oldItem == newItem
}