package ba.grbo.weatherchecker.ui.adapters

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import ba.grbo.weatherchecker.R
import ba.grbo.weatherchecker.util.OnImageLoadingError
import coil.load
import com.balysv.materialripple.MaterialRippleLayout

@BindingAdapter("startDrawable", "endDrawable", requireAll = true)
fun TextView.bindStartAndEndDrawable(
    @DrawableRes startDrawable: Int,
    @DrawableRes endDrawable: Int
) {
    setCompoundDrawablesWithIntrinsicBounds(startDrawable, 0, endDrawable, 0)
}

@BindingAdapter("iconCode", "onError", requireAll = true)
fun ImageView.bindLoadedImage(iconCode: String, onError: OnImageLoadingError) {
    load("https://www.openweathermap.org/img/wn/$iconCode@2x.png") {
        crossfade(true)
        placeholder(R.drawable.ic_loading)
        listener(onError = { _, throwable -> onError.onError(throwable) })
    }
}

@BindingAdapter("imgSrc")
fun ImageView.bindImage(@DrawableRes imgSrc: Int) {
    setImageResource(imgSrc)
}

@BindingAdapter("customRippleColor")
fun MaterialRippleLayout.bindRippleColor(color: Int) {
    setRippleColor(color)
}