package ba.grbo.weatherchecker.util

import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.text.TextPaint
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.weatherchecker.R

// Credits go to the author of this repository https://github.com/xabaras/RecyclerViewSwipeDecorator
// as I took his code, converted it to Kotlin and modified it so it draws rounded corners.
class RecyclerViewSwipeDecorator(
    private val canvas: Canvas,
    private val recyclerView: RecyclerView,
    private val viewHolder: RecyclerView.ViewHolder,
    private val dX: Float,
    private val actionState: Int,
) {

    private val swipeBackgroundColor = ContextCompat.getColor(
        recyclerView.context,
        R.color.swipe_background
    )

    private val iconHorizontalMargin = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        16f,
        recyclerView.context.resources.displayMetrics
    ).toInt()

    private val swipeIconId = R.drawable.ic_delete
    private val swipeText = "Delete"
    private val swipeTextSize = 18f
    private val swipeTextUnit = TypedValue.COMPLEX_UNIT_SP
    private val swipeTextColor = ContextCompat.getColor(
        recyclerView.context,
        R.color.swipe_text_and_icon
    )
    private val swipeTextTypeface = Typeface.SANS_SERIF

    private val cornerRadius = 8f.toPixels(recyclerView.context.resources)

    fun decorate() {
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return
        if (dX > 0) {
            // Swiping Right
            canvas.clipRect(
                viewHolder.itemView.left,
                viewHolder.itemView.top,
                viewHolder.itemView.left + dX.toInt(),
                viewHolder.itemView.bottom
            )
            if (swipeBackgroundColor != 0) {
                GradientDrawable().apply {
                    setColor(swipeBackgroundColor)
                    setBounds(
                        viewHolder.itemView.left,
                        viewHolder.itemView.top,
                        viewHolder.itemView.left + dX.toInt(),
                        viewHolder.itemView.bottom
                    )
                    cornerRadius = this@RecyclerViewSwipeDecorator.cornerRadius
                }.draw(canvas)

            }
            var iconSize = 0
            if (swipeIconId != 0 && dX > iconHorizontalMargin) {
                val icon =
                    ContextCompat.getDrawable(recyclerView.context, swipeIconId)
                if (icon != null) {
                    iconSize = icon.intrinsicHeight
                    val halfIcon = iconSize / 2
                    val top: Int =
                        viewHolder.itemView.top + ((viewHolder.itemView.bottom - viewHolder.itemView.top) / 2 - halfIcon)
                    icon.setBounds(
                        viewHolder.itemView.left + iconHorizontalMargin,
                        top,
                        viewHolder.itemView.left + iconHorizontalMargin + icon.intrinsicWidth,
                        top + icon.intrinsicHeight
                    )
                    icon.draw(canvas)
                }
            }
            if (swipeText.isNotEmpty() && dX > iconHorizontalMargin + iconSize) {
                val textPaint = TextPaint()
                textPaint.isAntiAlias = true
                textPaint.textSize =
                    TypedValue.applyDimension(
                        swipeTextUnit,
                        swipeTextSize,
                        recyclerView.context.resources.displayMetrics
                    )
                textPaint.color = swipeTextColor
                textPaint.typeface = swipeTextTypeface
                val textTop =
                    (viewHolder.itemView.top + (viewHolder.itemView.bottom - viewHolder.itemView.top) / 2.0 + textPaint.textSize / 2).toInt()
                canvas.drawText(
                    swipeText,
                    (viewHolder.itemView.left + iconHorizontalMargin + iconSize + if (iconSize > 0) iconHorizontalMargin / 2 else 0).toFloat(),
                    textTop.toFloat(),
                    textPaint
                )
            }
        } else if (dX < 0) {
            // Swiping Left
            canvas.clipRect(
                viewHolder.itemView.right + dX.toInt(),
                viewHolder.itemView.top,
                viewHolder.itemView.right,
                viewHolder.itemView.bottom
            )
            if (swipeBackgroundColor != 0) {
                GradientDrawable().apply {
                    setColor(swipeBackgroundColor)
                    setBounds(
                        viewHolder.itemView.right + dX.toInt(),
                        viewHolder.itemView.top,
                        viewHolder.itemView.right,
                        viewHolder.itemView.bottom
                    )
                    cornerRadius = this@RecyclerViewSwipeDecorator.cornerRadius
                }.draw(canvas)

            }
            var iconSize = 0
            var imgLeft: Int = viewHolder.itemView.right
            if (swipeIconId != 0 && dX < -iconHorizontalMargin) {
                val icon =
                    ContextCompat.getDrawable(recyclerView.context, swipeIconId)
                if (icon != null) {
                    iconSize = icon.intrinsicHeight
                    val halfIcon = iconSize / 2
                    val top: Int =
                        viewHolder.itemView.top + ((viewHolder.itemView.bottom - viewHolder.itemView.top) / 2 - halfIcon)
                    imgLeft =
                        viewHolder.itemView.right - iconHorizontalMargin - halfIcon * 2
                    icon.setBounds(
                        imgLeft,
                        top,
                        viewHolder.itemView.right - iconHorizontalMargin,
                        top + icon.intrinsicHeight
                    )
                    icon.draw(canvas)
                }
            }
            if (swipeText.isNotEmpty() && dX < -iconHorizontalMargin - iconSize) {
                val textPaint = TextPaint()
                textPaint.isAntiAlias = true
                textPaint.textSize =
                    TypedValue.applyDimension(
                        swipeTextUnit,
                        swipeTextSize,
                        recyclerView.context.resources.displayMetrics
                    )
                textPaint.color = swipeTextColor
                textPaint.typeface = swipeTextTypeface
                val width = textPaint.measureText(swipeText)
                val textTop =
                    (viewHolder.itemView.top + (viewHolder.itemView.bottom - viewHolder.itemView.top) / 2.0 + textPaint.textSize / 2).toInt()
                canvas.drawText(
                    swipeText,
                    imgLeft - width - if (imgLeft == viewHolder.itemView.right) iconHorizontalMargin else iconHorizontalMargin / 2,
                    textTop.toFloat(),
                    textPaint
                )
            }
        }
    }
}