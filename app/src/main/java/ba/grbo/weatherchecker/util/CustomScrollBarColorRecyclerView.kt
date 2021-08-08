package ba.grbo.weatherchecker.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView


@Suppress("UNUSED", "DEPRECATION")
open class CustomScrollBarColorRecyclerView : RecyclerView {
    var scrollBarColor: Int = context.getColorFromAttribute(android.R.attr.textColorHint)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    // Has to be protected otherwise it doesn't work
    protected fun onDrawHorizontalScrollBar(
        canvas: Canvas,
        scrollBar: Drawable,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
        // Using deprecated one, because the other one requires parameter that is API 29+
        scrollBar.setColorFilter(scrollBarColor, PorterDuff.Mode.SRC_ATOP)
        scrollBar.setBounds(l, t, r, b)
        scrollBar.draw(canvas)
    }

    protected fun onDrawVerticalScrollBar(
        canvas: Canvas,
        scrollBar: Drawable,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
        // Using deprecated one, because the other one requires parameter that is API 29+
        scrollBar.setColorFilter(scrollBarColor, PorterDuff.Mode.SRC_ATOP)
        scrollBar.setBounds(l, t, r, b)
        scrollBar.draw(canvas)
    }
}