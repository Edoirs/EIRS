package ng.gov.eirs.mas.erasmpoa.customviews

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import ng.gov.eirs.mas.erasmpoa.R

class AspectRatioLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var aspectRatio: Float = 1.0f // Default aspect ratio

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.AspectRatioLinearLayout)
            aspectRatio = typedArray.getFloat(R.styleable.AspectRatioLinearLayout_aspectRatio, 1.0f)
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (width / aspectRatio).toInt()
        val adjustedHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, adjustedHeightMeasureSpec)
    }
}
