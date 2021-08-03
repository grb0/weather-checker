package ba.grbo.weatherchecker.ui.adapters

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("startDrawable", "endDrawable", requireAll = true)
fun TextView.bindStartAndEndDrawable(startDrawable: Int, endDrawable: Int) {
    setCompoundDrawablesWithIntrinsicBounds(startDrawable, 0, endDrawable, 0)
}