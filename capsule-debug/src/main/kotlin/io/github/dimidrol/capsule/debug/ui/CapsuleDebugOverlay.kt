package io.github.dimidrol.capsule.debug.ui

import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import io.github.dimidrol.capsule.debug.CapsuleDebugConfig
import io.github.dimidrol.capsule.debug.CapsuleDebugRegistry
import java.util.WeakHashMap
import kotlin.math.hypot

internal class CapsuleDebugOverlay(
    private val registry: CapsuleDebugRegistry,
    private val config: CapsuleDebugConfig
) : Application.ActivityLifecycleCallbacks, AutoCloseable {
    private var horizontalPosition = 1f
    private var verticalPosition = 0.5f
    private val buttons = WeakHashMap<Activity, FloatingButtonBinding>()

    override fun onActivityResumed(activity: Activity) = attachButton(activity)
    override fun onActivityPaused(activity: Activity) = detachButton(activity)
    override fun onActivityDestroyed(activity: Activity) = detachButton(activity)
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun close() {
        buttons.keys.toList().forEach(::detachButton)
    }

    private fun attachButton(activity: Activity) {
        if (buttons.containsKey(activity)) return

        val root = activity.findViewById<FrameLayout>(android.R.id.content) ?: return
        val button = TextView(activity).apply {
            text = config.floatingButtonText.take(3)
            contentDescription = "Open Capsule state time travel"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            elevation = activity.dp(10).toFloat()
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(COLOR_ACCENT)
                setStroke(activity.dp(2), Color.WHITE)
            }
            setOnClickListener {
                CapsuleDebugInspectorDialog(activity, registry).show()
            }
        }
        val size = activity.dp(54)
        val layoutParams = FrameLayout.LayoutParams(size, size).apply {
            gravity = Gravity.START or Gravity.TOP
        }
        val edgePadding = activity.dp(8).toFloat()
        val layoutListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            placeAtSavedPosition(root, button, edgePadding)
        }

        root.addView(button, layoutParams)
        root.addOnLayoutChangeListener(layoutListener)
        installDragHandler(
            root = root,
            button = button,
            edgePadding = edgePadding,
            touchSlop = ViewConfiguration.get(activity).scaledTouchSlop.toFloat()
        )
        root.post { placeAtSavedPosition(root, button, edgePadding) }
        buttons[activity] = FloatingButtonBinding(root, button, layoutListener)
    }

    private fun detachButton(activity: Activity) {
        val binding = buttons.remove(activity) ?: return
        binding.root.removeOnLayoutChangeListener(binding.layoutListener)
        (binding.button.parent as? ViewGroup)?.removeView(binding.button)
    }

    private fun installDragHandler(
        root: FrameLayout,
        button: View,
        edgePadding: Float,
        touchSlop: Float
    ) {
        var downRawX = 0f
        var downRawY = 0f
        var downButtonX = 0f
        var downButtonY = 0f
        var dragging = false

        button.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    downButtonX = view.x
                    downButtonY = view.y
                    dragging = false
                    view.isPressed = true
                    view.parent?.requestDisallowInterceptTouchEvent(true)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - downRawX
                    val deltaY = event.rawY - downRawY
                    if (!dragging && hypot(deltaX, deltaY) >= touchSlop) dragging = true
                    if (dragging) {
                        moveInsideBounds(
                            root,
                            view,
                            downButtonX + deltaX,
                            downButtonY + deltaY,
                            edgePadding
                        )
                        savePosition(root, view, edgePadding)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    view.parent?.requestDisallowInterceptTouchEvent(false)
                    if (dragging) savePosition(root, view, edgePadding) else view.performClick()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    view.isPressed = false
                    view.parent?.requestDisallowInterceptTouchEvent(false)
                    true
                }
                else -> false
            }
        }
    }

    private fun placeAtSavedPosition(root: FrameLayout, button: View, edgePadding: Float) {
        val horizontalRange = availableRange(root.width, button.width, edgePadding)
        val verticalRange = availableRange(root.height, button.height, edgePadding)
        moveInsideBounds(
            root,
            button,
            horizontalRange.start + horizontalRange.length * horizontalPosition,
            verticalRange.start + verticalRange.length * verticalPosition,
            edgePadding
        )
    }

    private fun moveInsideBounds(
        root: FrameLayout,
        button: View,
        requestedX: Float,
        requestedY: Float,
        edgePadding: Float
    ) {
        val horizontalRange = availableRange(root.width, button.width, edgePadding)
        val verticalRange = availableRange(root.height, button.height, edgePadding)
        button.x = requestedX.coerceIn(horizontalRange.start, horizontalRange.end)
        button.y = requestedY.coerceIn(verticalRange.start, verticalRange.end)
    }

    private fun savePosition(root: FrameLayout, button: View, edgePadding: Float) {
        val horizontalRange = availableRange(root.width, button.width, edgePadding)
        val verticalRange = availableRange(root.height, button.height, edgePadding)
        horizontalPosition = horizontalRange.fractionFor(button.x)
        verticalPosition = verticalRange.fractionFor(button.y)
    }

    private fun availableRange(
        containerSize: Int,
        viewSize: Int,
        edgePadding: Float
    ): PositionRange {
        val end = (containerSize - viewSize - edgePadding).coerceAtLeast(edgePadding)
        return PositionRange(edgePadding, end)
    }

    private fun Activity.dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private companion object {
        val COLOR_ACCENT: Int = Color.rgb(218, 75, 43)
    }
}
