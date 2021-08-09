package ba.grbo.weatherchecker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.weatherchecker.data.models.local.Place
import ba.grbo.weatherchecker.databinding.SuggestedPlaceBinding

class SuggestedPlaceAdapter(
    private val onSuggestedPlacesChanged: () -> Unit,
    private val onClick: (Place) -> Unit,
    var rippleColor: Int
) : ListAdapter<Place, SuggestedPlaceAdapter.SuggestedPlaceHolder>(PlaceDiffCallbacks()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedPlaceHolder {
        return SuggestedPlaceHolder.from(parent, rippleColor)
    }

    override fun onBindViewHolder(holder: SuggestedPlaceHolder, position: Int) {
        holder.bind(getItem(position), onClick, rippleColor)
    }

    class SuggestedPlaceHolder private constructor(
        private val binding: SuggestedPlaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup, rippleColor: Int) = SuggestedPlaceHolder(
                SuggestedPlaceBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ).apply { suggestedPlaceRippleLayout.setRippleColor(rippleColor) }
            )
        }

        fun bind(place: Place, onClick: (Place) -> Unit, rippleColor: Int) {
            binding.place = place
            binding.hasInternet = false
            binding.suggestedPlaceRippleLayout.setRippleColor(rippleColor)
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