package io.github.dimidrol.capsule.debug.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

internal class CapsuleDebugTextAdapter(
    context: Context,
    layoutResource: Int,
    private val colors: CapsuleDebugColors
) : ArrayAdapter<String>(context, layoutResource, mutableListOf()) {
    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        style(super.getView(position, convertView, parent))

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
        style(super.getDropDownView(position, convertView, parent))

    private fun style(view: View): View = view.apply {
        setBackgroundColor(colors.surface)
        findViewById<TextView>(android.R.id.text1)?.setTextColor(colors.text)
    }
}
