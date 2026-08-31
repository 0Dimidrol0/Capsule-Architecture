package io.github.dimidrol.capsule.debug.ui

import android.view.View
import android.widget.FrameLayout

internal data class FloatingButtonBinding(
    val root: FrameLayout,
    val button: View,
    val layoutListener: View.OnLayoutChangeListener
)
