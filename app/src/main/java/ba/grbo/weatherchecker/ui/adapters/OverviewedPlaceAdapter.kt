package ba.grbo.weatherchecker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.weatherchecker.data.models.local.Place
import ba.grbo.weatherchecker.databinding.OverviewedPlaceBinding
import ba.grbo.weatherchecker.util.OnImageLoadingError

class OverviewedPlaceAdapter(
    private val onOverviewedPlacesChanged: () -> Unit,
    private val onImageLoadingError: OnImageLoadingError
) : ListAdapter<Place, OverviewedPlaceAdapter.OverviewedPlaceHolder>(PlaceDiffCallbacks()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OverviewedPlaceHolder = OverviewedPlaceHolder.from(parent)

    override fun onBindViewHolder(holder: OverviewedPlaceHolder, position: Int) {
        holder.bind(getItem(position), onImageLoadingError)
    }

    class OverviewedPlaceHolder private constructor(
        private val binding: OverviewedPlaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup) = OverviewedPlaceHolder(
                OverviewedPlaceBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        fun bind(place: Place, onImageLoadingError: OnImageLoadingError) {
            binding.place = place
            binding.onImageLoadingError = onImageLoadingError
            binding.executePendingBindings()
        }
    }

    override fun onCurrentListChanged(
        previousList: MutableList<Place>,
        currentList: MutableList<Place>
    ) {
        // Only if the items were added
        if (currentList.size > previousList.size) onOverviewedPlacesChanged()
    }
}