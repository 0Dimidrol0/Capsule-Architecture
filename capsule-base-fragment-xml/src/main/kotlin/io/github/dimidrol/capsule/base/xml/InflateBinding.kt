package io.github.dimidrol.capsule.base.xml

import android.view.LayoutInflater
import android.view.ViewGroup

/** Inflate binding instance for a fragment view. */
typealias InflateBinding<Binding> = (LayoutInflater, ViewGroup?, Boolean) -> Binding
