package com.yalantis.ucrop.view.widget

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.yalantis.ucrop.R
import com.yalantis.ucrop.model.AspectRatio
import com.yalantis.ucrop.view.CropImageView
import java.util.Locale

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 */
class AspectRatioTextView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private val MARGIN_MULTIPLIER = 1.5f
    private val mCanvasClipBounds = Rect()
    private var mDotPaint: Paint? = null
    private var mDotSize = 0
    private var mAspectRatio = 0f
    private var mAspectRatioTitle: String? = null
    private var mAspectRatioX = 0f
    private var mAspectRatioY = 0f

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0) {
    }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_AspectRatioTextView)
        init(a)
    }

    /**
     * @param activeColor the resolved color for active elements
     */
    fun setActiveColor(
        @ColorInt activeColor: Int,
    ) {
        applyActiveColor(activeColor)
        invalidate()
    }

    fun setAspectRatio(aspectRatio: AspectRatio) {
        mAspectRatioTitle = aspectRatio.aspectRatioTitle
        mAspectRatioX = aspectRatio.aspectRatioX
        mAspectRatioY = aspectRatio.aspectRatioY
        mAspectRatio =
            if (mAspectRatioX == CropImageView.SOURCE_IMAGE_ASPECT_RATIO || mAspectRatioY == CropImageView.SOURCE_IMAGE_ASPECT_RATIO) {
                CropImageView.SOURCE_IMAGE_ASPECT_RATIO
            } else {
                mAspectRatioX / mAspectRatioY
            }
        setTitle()
    }

    fun getAspectRatio(toggleRatio: Boolean): Float {
        if (toggleRatio) {
            toggleAspectRatio()
            setTitle()
        }
        return mAspectRatio
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isSelected) {
            canvas.getClipBounds(mCanvasClipBounds)
            val x = (mCanvasClipBounds.right - mCanvasClipBounds.left) / 2.0f
            val y =
                mCanvasClipBounds.bottom - mCanvasClipBounds.top / 2f - mDotSize * MARGIN_MULTIPLIER
            canvas.drawCircle(x, y, mDotSize / 2f, mDotPaint!!)
        }
    }

    private fun init(a: TypedArray) {
        gravity = Gravity.CENTER_HORIZONTAL
        mAspectRatioTitle =
            a.getString(R.styleable.ucrop_AspectRatioTextView_ucrop_artv_ratio_title)
        mAspectRatioX =
            a.getFloat(
                R.styleable.ucrop_AspectRatioTextView_ucrop_artv_ratio_x,
                CropImageView.SOURCE_IMAGE_ASPECT_RATIO,
            )
        mAspectRatioY =
            a.getFloat(
                R.styleable.ucrop_AspectRatioTextView_ucrop_artv_ratio_y,
                CropImageView.SOURCE_IMAGE_ASPECT_RATIO,
            )
        mAspectRatio =
            if (mAspectRatioX == CropImageView.SOURCE_IMAGE_ASPECT_RATIO || mAspectRatioY == CropImageView.SOURCE_IMAGE_ASPECT_RATIO) {
                CropImageView.SOURCE_IMAGE_ASPECT_RATIO
            } else {
                mAspectRatioX / mAspectRatioY
            }
        mDotSize = context.resources.getDimensionPixelSize(R.dimen.ucrop_size_dot_scale_text_view)
        mDotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mDotPaint!!.style = Paint.Style.FILL
        setTitle()
        val activeColor = ContextCompat.getColor(this.context, R.color.ucrop_color_widget_active)
        applyActiveColor(activeColor)
        a.recycle()
    }

    private fun applyActiveColor(
        @ColorInt activeColor: Int,
    ) {
        if (mDotPaint != null) {
            mDotPaint!!.color = activeColor
        }
        val textViewColorStateList =
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf(0)),
                intArrayOf(
                    activeColor,
                    ContextCompat.getColor(context, R.color.ucrop_color_widget),
                ),
            )
        setTextColor(textViewColorStateList)
    }

    private fun toggleAspectRatio() {
        if (mAspectRatio != CropImageView.SOURCE_IMAGE_ASPECT_RATIO) {
            val tempRatioW = mAspectRatioX
            mAspectRatioX = mAspectRatioY
            mAspectRatioY = tempRatioW
            mAspectRatio = mAspectRatioX / mAspectRatioY
        }
    }

    private fun setTitle() {
        text =
            if (!TextUtils.isEmpty(mAspectRatioTitle)) {
                mAspectRatioTitle
            } else {
                String.format(
                    Locale.US,
                    "%d:%d",
                    mAspectRatioX.toInt(),
                    mAspectRatioY.toInt(),
                )
            }
    }
}
