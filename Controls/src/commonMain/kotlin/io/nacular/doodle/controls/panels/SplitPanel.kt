package io.nacular.doodle.controls.panels

import io.nacular.doodle.controls.theme.SplitPanelBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.layout.fill
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Vertical
import io.nacular.doodle.utils.observable


public class SplitPanel(orientation: Orientation = Vertical, ratio: Float = 0.5f): View() {

    public var behavior: SplitPanelBehavior? by behavior { _,new ->
        divider?.let { children -= it }

        new?.also { behavior ->
            divider = behavior.divider(this)

            divider?.let {
                if (behavior.dividerVisible) {
                    panelSpacing = it.width
                }

                children += it

                it.zOrder = 1
            }

            if (divider != null) {
                updateLayout()
            }
        }
    }

    public var firstItem: View? by observable(null) { old,new ->
        old?.let { children -= it }
        new?.let { children += it }

        updateLayout    ()
        contentsChanged_()
        fireLegacyChanged()
    }

    public var lastItem: View? by observable(null) { old,new ->
        old?.let { children -= it }
        new?.let { children += it }

        updateLayout()
        contentsChanged_()
        fireLegacyChanged()
    }

    @Suppress("PrivatePropertyName")
    private val contentsChanged_ = ChangeObserversImpl(this)

    public val contentsChanged: ChangeObservers<SplitPanel> = contentsChanged_

    @Suppress("PrivatePropertyName")
    private val orientationChanged_ = ChangeObserversImpl(this)

    public val orientationChanged: ChangeObservers<SplitPanel> = orientationChanged_

    public var orientation: Orientation by observable(orientation) { _,_ ->
        updateLayout()
        orientationChanged_()
        fireLegacyChanged()
    }

    // FIXME: Remove in 0.7.0
    @Deprecated("Remove in 0.7.0")
    private fun fireLegacyChanged() {
        changed_()
    }

    public var ratio: Float = ratio; set(new) { if (new != field) { field = new; relayout(); changed_() } }

    private var divider      = null as View?
    private var panelSpacing = 0.0

    @Suppress("PrivatePropertyName")
    private val changed_ = ChangeObserversImpl(this)

    public val changed: ChangeObservers<SplitPanel> = changed_

    public override var insets: Insets
        get(   ) = super.insets
        set(new) { if (new != super.insets) { super.insets = new; relayout() } }

    init {
        require(ratio in 0.0f .. 1.0f) { "ratio must be in 0 .. 1" }
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point): Boolean = super.contains(point) && behavior?.contains(this, point) ?: true

    @Suppress("NAME_SHADOWING")
    private fun updateLayout() {
        val first   = firstItem
        val last    = lastItem
        val divider = divider

        val layout = when {
            first != null && last != null -> {
                constrain(first, last) { first, last ->
                    when (orientation) {
                        Vertical -> {
                            first.top    = parent.top    + { insets.top    }
                            first.left   = parent.left   + { insets.left   }
                            first.bottom = parent.bottom - { insets.bottom }
                            first.right  = first.left + (parent.width - { panelSpacing + insets.left + insets.right }) * { ratio }
                            last.top     = first.top
                            last.left    = first.right + { panelSpacing }
                            last.bottom  = first.bottom
                            last.right   = parent.right - { insets.right }
                        }
                        else -> {
                            first.top    = parent.top    + { insets.top    }
                            first.left   = parent.left   + { insets.left   }
                            first.bottom = first.top + (parent.height - { panelSpacing + insets.top + insets.bottom }) * { ratio }
                            first.right  = parent.right - { insets.right }
                            last.top     = first.bottom + { panelSpacing }
                            last.left    = first.left
                            last.right   = first.right
                            last.bottom  = parent.bottom - { insets.bottom }
                        }
                    }
                }
            }

            first != null -> constrain(first) { fill(it) }
            last  != null -> constrain(last ) { fill(it) }
            else -> null
        }

        layout?.let {
            if (divider != null && first != null) {
                it.constrain(divider, first) { divider, first ->
                    when (orientation) {
                        Vertical -> {
                            divider.top     = first.top
                            divider.bottom  = first.bottom
                            divider.centerX = parent.left + { insets.left } + (divider.parent.width - { panelSpacing + insets.left + insets.right }) * { ratio }
                        }
                        else -> {
                            divider.left    = first.left
                            divider.right   = first.right
                            divider.centerY = parent.top + { insets.top } + (divider.parent.height - { panelSpacing + insets.top + insets.bottom }) * { ratio }
                        }
                    }
                }
            }
        }

        this.layout = layout
    }
}