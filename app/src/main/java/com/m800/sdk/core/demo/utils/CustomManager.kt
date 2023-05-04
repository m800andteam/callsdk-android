package com.m800.sdk.core.demo.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.*
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.databinding.BindingAdapter
import com.m800.sdk.core.demo.R
import com.m800.sdk.core.demo.utils.CustomManager.getColorAlert

object CustomManager {
    const val ALPHA_5 = 0.05
    const val ALPHA_10 = 0.1
    const val ALPHA_20 = 0.2
    const val ALPHA_40 = 0.4f
    const val ALPHA_50 = 0.5
    const val ALPHA_90 = 0.9
    const val ALPHA_80 = 0.8
    const val ALPHA_100 = 1.0f

    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }

    // === Color ===

    @JvmStatic
    @JvmOverloads
    fun getColorSecondary(alpha: Double = 1.0): Int {
        return getColorById(R.color.secondary, alpha)
    }

    @JvmStatic
    @JvmOverloads
    fun getColorBackgroundLight(alpha: Double = 1.0): Int {
        return getColorById(R.color.background_light, alpha)
    }

    @JvmStatic
    @JvmOverloads
    fun getColorTitleBlack(alpha: Double = 1.0): Int {
        return getColorById(R.color.title_black, alpha)
    }

    @JvmStatic
    @JvmOverloads
    fun getColorBlack(alpha: Double = 1.0): Int {
        return getColorById(R.color.black, alpha)
    }

    @JvmStatic
    @JvmOverloads
    fun getColorWhite(alpha: Double = 1.0): Int {
        return getColorById(R.color.white, alpha)
    }

    @JvmStatic
    @JvmOverloads
    fun getColorExternal(alpha: Double = 1.0): Int {
        return getColorById(R.color.external, alpha)
    }

    @JvmStatic
    @JvmOverloads
    fun getColorAlert(alpha: Double = 1.0): Int {
        return getColorById(R.color.alert, alpha)
    }

    @JvmStatic
    @JvmOverloads
    fun getColorCall(alpha: Double = 1.0): Int {
        return getColorById(R.color.call, alpha)
    }

    @JvmStatic
    @JvmOverloads
    fun getColorIcon(alpha: Double = 1.0): Int {
        return getColorById(R.color.icon, alpha)
    }

    @JvmStatic
    @JvmOverloads
    fun getColorAccentuateDark(): Int {
        return getColorBlack(ALPHA_90)
    }

    @JvmStatic
    @JvmOverloads
    fun getColorBodyBlack(alpha: Double = 1.0): Int {
        return getColorById(R.color.body_black, alpha)
    }

    @JvmStatic
    @JvmOverloads
    fun getColorDivider(alpha: Double = 1.0): Int {
        return getColorById(R.color.divider, alpha)
    }

    private fun getColorById(colorId: Int, alpha: Double): Int {
        val color = ContextCompat.getColor(context, colorId)
        return if (alpha < 1.0) {
            ColorUtils.setAlphaComponent(color, (alpha * 255).toInt())
        } else {
            color
        }
    }

    // === Ripple ===

    @JvmStatic
    @JvmOverloads
    fun getColorRippleWithMask(colorRipple: Int, @DrawableRes resId: Int): RippleDrawable {
        return RippleDrawable(ColorStateList.valueOf(colorRipple), null, context.getDrawable(resId))
    }

    @JvmStatic
    fun getColorSelectorLcBgHold(): StateListDrawable {
        val stateListDrawable = context.resources.getDrawable(R.drawable.lc_bg_hold) as StateListDrawable
        stateListDrawable.setState(context)
        return stateListDrawable
    }

    @JvmStatic
    fun getColorSelectorLcBgMute(): StateListDrawable {
        val stateListDrawable = context.resources.getDrawable(R.drawable.lc_bg_mute) as StateListDrawable
        stateListDrawable.setState(context)
        return stateListDrawable
    }

    @JvmStatic
    fun getColorSelectorDeclineButton(): StateListDrawable {
        val stateListDrawable = context.resources.getDrawable(R.drawable.lc_call_decline_button_selector) as StateListDrawable
        stateListDrawable.setState(context)
        return stateListDrawable
    }

    @JvmStatic
    fun getColorSelectorAcceptButton(): StateListDrawable {
        val stateListDrawable = context.resources.getDrawable(R.drawable.lc_call_accept_button_selector) as StateListDrawable
        stateListDrawable.setState(context)
        return stateListDrawable
    }

    // === Inset ===
    @JvmStatic
    @JvmOverloads
    fun getInsetDrawable(resId: Int): InsetDrawable {
        return context.resources.getDrawable(resId) as InsetDrawable
    }

    @JvmStatic
    @JvmOverloads
    fun getCustomAnimation(images: List<Int>, duration: Int, tintColor: Int? = null): AnimationDrawable ? {
        val animationDrawable = AnimationDrawable()

        images.forEach { image ->
            val drawable = context.resources.getDrawable(image)
            drawable?.let {
                it.apply {
                    tintColor?.let {
                        when (tintColor) {
                            R.color.black -> {
                                setColorFilter(getColorBlack(), PorterDuff.Mode.SRC_ATOP)
                            }
                        }
                    }
                }
                animationDrawable.addFrame(it, duration)
            }

            if (drawable is VectorDrawable) {
                drawable.let {
                    it.apply {
                        tintColor?.let {
                            setColorFilter(it, PorterDuff.Mode.SRC_ATOP)
                        }
                    }
                    animationDrawable.addFrame(it, duration)
                }
            } else {
            }
        }

        return animationDrawable
    }
}

@BindingAdapter("app:bindInset")
fun bindInset(linearLayout: LinearLayout, resId: Int) {
    val drawable = CustomManager.getInsetDrawable(resId)
    linearLayout.setBackgroundDrawable(drawable)
}

fun CheckBox.bindLayerList(resId: Int) {
    val layerDrawable = this.context.resources.getDrawable(resId) as LayerDrawable
    layerDrawable.setLayerList(this.context)
    this.setBackground(layerDrawable)
}

fun StateListDrawable.setState(context: Context) {
    for (index in 0 until this.stateCount) {
        val layerDrawable = this.getStateDrawable(index) as LayerDrawable
        layerDrawable.setLayerList(context)
        this.addState(this.getStateSet(index), layerDrawable)
    }
}

fun LayerDrawable.setLayerList(context: Context, colorMap: HashMap<Int, Int>? = null) {
    for (index in 0 until this.numberOfLayers) {
        val id = this.getId(index)
        if (id > 0) {
            val drawable = this.findDrawableByLayerId(id)
            if (drawable is GradientDrawable) {
                this.setDrawableByLayerId(id, drawable.setColorById(id))
                if (colorMap != null && colorMap.containsKey(id)) {
                    drawable.setColor(colorMap.get(id)!!)
                    this.setDrawableByLayerId(id, drawable)
                }
            }
            if (drawable is VectorDrawable) {
                if (colorMap != null && colorMap.containsKey(id)) {
                    this.setDrawableByLayerId(id, drawable.setImage(context, id).apply {
                        setColorFilter(colorMap.get(id)!!, PorterDuff.Mode.SRC_ATOP)
                    })
                } else {
                    this.setDrawableByLayerId(id, drawable.setImage(context, id).setColor(id))
                }
            }
            if (drawable is InsetDrawable) {
                this.setDrawableByLayerId(id, drawable.setColorById(id))
            }
        }
    }
}

fun InsetDrawable.setColorById(id: Int): InsetDrawable {
    return this.apply {
        when (id) {
            R.id.alert -> {
                setColorFilter(getColorAlert(), PorterDuff.Mode.SRC_ATOP)
            }
        }
    }
}

fun VectorDrawable.setColor(id: Int): VectorDrawable {
    return this.apply {
        when (id) {
            R.id.call -> {
                setColorFilter(CustomManager.getColorCall(), PorterDuff.Mode.SRC_ATOP)
            }
            R.id.accentuate_dark, R.id.call_panel_hold_dark, R.id.call_panel_unmute -> {
                setColorFilter(CustomManager.getColorAccentuateDark(), PorterDuff.Mode.SRC_ATOP)
            }
            R.id.alert, R.id.call_panel_mute_line -> {
                setColorFilter(CustomManager.getColorAlert(), PorterDuff.Mode.SRC_ATOP)
            }
            R.id.external -> {
                setColorFilter(CustomManager.getColorExternal(), PorterDuff.Mode.SRC_ATOP)
            }
            R.id.icon -> {
                setColorFilter(CustomManager.getColorIcon(), PorterDuff.Mode.SRC_ATOP)
            }
            R.id.white, R.id.call_panel_audio_speaker_light, R.id.call_panel_end,
            R.id.avatar_inner_contact, R.id.call_panel_hold_white, R.id.call_panel_mute_main,
            R.id.call_button_accept -> {
                setColorFilter(CustomManager.getColorWhite(), PorterDuff.Mode.SRC_ATOP)
            }
        }
    }
}

fun VectorDrawable.setImage(context: Context, id: Int): VectorDrawable {
    return ContextCompat.getDrawable(
        context,
        when (id) {
            R.id.call_panel_audio_bluetooth -> {
                R.drawable.call_panel_audio_bluetooth
            }
            R.id.call_panel_audio_speaker_dark -> {
                R.drawable.call_panel_audio_speaker_dark
            }
            R.id.call_panel_audio_speaker_light -> {
                R.drawable.call_panel_audio_speaker_light
            }
            R.id.call_panel_end -> {
                R.drawable.call_panel_end
            }
            R.id.avatar_inner_contact -> {
                R.drawable.avatar_inner_contact
            }
            R.id.call_panel_hold_dark -> {
                R.drawable.call_panel_hold
            }
            R.id.call_panel_hold_white -> {
                R.drawable.call_panel_hold
            }
            R.id.call_panel_unmute -> {
                R.drawable.call_panel_unmute
            }
            R.id.call_panel_mute_main -> {
                R.drawable.call_panel_mute_main
            }
            R.id.call_panel_mute_line -> {
                R.drawable.call_panel_mute_line
            }
            R.id.call_button_accept -> {
                R.drawable.call_button_accept
            }
            else -> {
                return this
            }
        }
    ) as? VectorDrawable ?: this
}

private fun GradientDrawable.setColorById(id: Int): GradientDrawable {
    return this.apply {
        when (id) {
            R.id.external -> {
                setColor(CustomManager.getColorExternal())
            }
            R.id.white -> {
                setColor(CustomManager.getColorWhite())
            }
        }
    }
}
