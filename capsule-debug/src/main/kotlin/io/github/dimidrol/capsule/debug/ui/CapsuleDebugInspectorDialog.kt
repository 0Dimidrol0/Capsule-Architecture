package io.github.dimidrol.capsule.debug.ui

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import io.github.dimidrol.capsule.debug.CapsuleDebugRegistry
import io.github.dimidrol.capsule.debug.CapsuleDebugSession
import io.github.dimidrol.capsule.debug.CapsuleStateSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class CapsuleDebugInspectorDialog(
    private val activity: Activity,
    private val registry: CapsuleDebugRegistry
) {
    private val colors = CapsuleDebugColors.from(activity)
    private val dialog = Dialog(
        activity,
        if (colors.isDark) {
            android.R.style.Theme_Material_NoActionBar
        } else {
            android.R.style.Theme_Material_Light_NoActionBar
        }
    )
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val sessionAdapter = CapsuleDebugTextAdapter(
        activity,
        android.R.layout.simple_spinner_item,
        colors
    )
    private val snapshotAdapter = CapsuleDebugTextAdapter(
        activity,
        android.R.layout.simple_list_item_1,
        colors
    )
    private val sessions = mutableListOf<CapsuleDebugSession>()
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private var snapshots = emptyList<CapsuleStateSnapshot>()
    private var selectedSession: CapsuleDebugSession? = null
    private var snapshotJob: Job? = null
    private var modeJob: Job? = null
    private lateinit var status: TextView
    private lateinit var screenSpinner: Spinner
    private lateinit var snapshotList: ListView
    private lateinit var liveButton: Button
    private lateinit var clearButton: Button

    fun show() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(buildContent())
        dialog.setOnDismissListener { scope.cancel() }
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (activity.resources.displayMetrics.widthPixels * 0.92f).toInt(),
                WindowManager.LayoutParams.MATCH_PARENT
            )
            attributes = attributes.apply { dimAmount = 0.55f }
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        observeSessions()
    }

    private fun buildContent(): View {
        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(16))
            background = roundedBackground(colors.surface, 22)
        }
        root.addView(TextView(activity).apply {
            text = "Capsule Time Travel"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colors.text)
        })
        root.addView(TextView(activity).apply {
            text = "Inspect a previous UI state without mutating the live runtime."
            textSize = 13f
            setTextColor(colors.muted)
            setPadding(0, dp(4), 0, dp(12))
        })

        screenSpinner = Spinner(activity).apply {
            adapter = sessionAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectSession(sessions.getOrNull(position))
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectSession(null)
                }
            }
        }
        root.addView(screenSpinner, matchWrapParams())

        status = TextView(activity).apply {
            textSize = 12f
            setTextColor(colors.accent)
            typeface = Typeface.MONOSPACE
            setPadding(0, dp(8), 0, dp(8))
        }
        root.addView(status)

        val actions = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        liveButton = actionButton("LIVE").apply {
            setOnClickListener { selectedSession?.resumeLive() }
        }
        clearButton = actionButton("CLEAR HISTORY").apply {
            setOnClickListener { selectedSession?.clearHistory() }
        }
        val closeButton = actionButton("CLOSE").apply {
            setOnClickListener { dialog.dismiss() }
        }
        actions.addView(liveButton, weightedActionParams())
        actions.addView(clearButton, weightedActionParams())
        actions.addView(closeButton, weightedActionParams())
        root.addView(actions)

        val empty = TextView(activity).apply {
            text = "No Capsule screen is active yet."
            gravity = Gravity.CENTER
            setTextColor(colors.muted)
            textSize = 14f
        }
        root.addView(empty, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(100)))

        snapshotList = ListView(activity).apply {
            adapter = snapshotAdapter
            divider = ColorDrawable(colors.divider)
            dividerHeight = dp(1)
            emptyView = empty
            setBackgroundColor(colors.surface)
            setOnItemClickListener { _, _, position, _ ->
                snapshots.getOrNull(position)?.let { snapshot ->
                    selectedSession?.preview(snapshot.id)
                }
            }
        }
        root.addView(
            snapshotList,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply {
                topMargin = dp(8)
            }
        )
        return root
    }

    private fun observeSessions() {
        scope.launch {
            registry.sessions.collectLatest { activeSessions ->
                val previousId = selectedSession?.id
                sessions.clear()
                sessions.addAll(activeSessions)
                sessionAdapter.clear()
                sessionAdapter.addAll(activeSessions.map { it.name + "  #" + it.id })
                sessionAdapter.notifyDataSetChanged()

                val selectedIndex = activeSessions.indexOfFirst { it.id == previousId }
                    .takeIf { it >= 0 }
                    ?: activeSessions.lastIndex
                if (selectedIndex >= 0) {
                    screenSpinner.setSelection(selectedIndex)
                    selectSession(activeSessions[selectedIndex])
                } else {
                    selectSession(null)
                }
            }
        }
    }

    private fun selectSession(session: CapsuleDebugSession?) {
        if (selectedSession?.id == session?.id && snapshotJob?.isActive == true) return
        selectedSession = session
        snapshotJob?.cancel()
        modeJob?.cancel()
        liveButton.isEnabled = session != null
        clearButton.isEnabled = session != null

        if (session == null) {
            snapshots = emptyList()
            snapshotAdapter.clear()
            snapshotAdapter.notifyDataSetChanged()
            status.text = "NO ACTIVE SCREEN"
            return
        }
        snapshotJob = scope.launch {
            session.snapshots.collectLatest { history ->
                snapshots = history.asReversed()
                snapshotAdapter.clear()
                snapshotAdapter.addAll(snapshots.map(::snapshotLabel))
                snapshotAdapter.notifyDataSetChanged()
                updateStatus()
            }
        }
        modeJob = scope.launch {
            session.stateMode.collectLatest { updateStatus() }
        }
    }

    private fun updateStatus() {
        val session = selectedSession ?: return
        status.text = session.stateMode.value.name.uppercase() +
            " | " + session.snapshots.value.size + " SNAPSHOTS"
    }

    private fun snapshotLabel(snapshot: CapsuleStateSnapshot): String =
        "#" + snapshot.sequence + "  " + timeFormat.format(Date(snapshot.capturedAtMillis)) +
            "\n" + snapshot.renderedState

    private fun actionButton(label: String): Button = Button(activity).apply {
        text = label
        textSize = 11f
        isAllCaps = false
        setTextColor(Color.WHITE)
        background = roundedBackground(colors.action, 12)
    }

    private fun matchWrapParams() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private fun weightedActionParams() = LinearLayout.LayoutParams(0, dp(46), 1f).apply {
        marginEnd = dp(6)
    }

    private fun roundedBackground(color: Int, radiusDp: Int) = GradientDrawable().apply {
        cornerRadius = dp(radiusDp).toFloat()
        setColor(color)
    }

    private fun dp(value: Int): Int =
        (value * activity.resources.displayMetrics.density).toInt()
}
