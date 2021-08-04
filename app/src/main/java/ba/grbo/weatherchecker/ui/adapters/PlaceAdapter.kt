package ba.grbo.weatherchecker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.weatherchecker.data.models.local.Place
import ba.grbo.weatherchecker.databinding.SuggestionItemBinding

class PlaceAdapter(
    private val onSuggestedPlacesChanged: () -> Unit,
    var rippleColor: Int
) : ListAdapter<Place, PlaceAdapter.PlaceHolder>(PlaceDiffCallbacks()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        return PlaceHolder.from(parent, rippleColor)
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.bind(getItem(position), rippleColor)
    }

    class PlaceHolder private constructor(
        private val binding: SuggestionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup, rippleColor: Int) = PlaceHolder(
                SuggestionItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ).apply { suggestionRippleLayout.setRippleColor(rippleColor) }
            )
        }

        fun bind(place: Place, rippleColor: Int) {
            binding.place = place
            binding.hasInternet = false
            binding.suggestionRippleLayout.setRippleColor(rippleColor)
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