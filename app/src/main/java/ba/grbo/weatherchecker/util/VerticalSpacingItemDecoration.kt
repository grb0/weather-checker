package ba.grbo.weatherchecker.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalSpacingItemDecoration(
    private val spacing: Int,
    private val isPortrait: Boolean
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (isPortrait) {
            outRect.left = spacing
            outRect.right = spacing
            outRect.bottom = spacing
        } else {
            val position = parent.getChildAdapterPosition(view)
            if (position % 2 == 0) {
                outRect.left = spacing
                outRect.right = spacing / 2
            } else {
                outRect.left = spacing / 2
                outRect.right = spacing
            }
            outRect.bottom = spacing
        }
    }
}
