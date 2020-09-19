package io.nacular.doodle.focus

import io.nacular.doodle.core.View
import io.nacular.doodle.utils.PropertyObservers

/**
 * Created by Nicholas Eddy on 3/2/18.
 */
interface FocusManager {
    val focusOwner    : View?
    val focusCycleRoot: View?
    val focusChanged  : PropertyObservers<FocusManager, View?>

    fun focusable(view: View): Boolean

    fun requestFocus      (view: View)
    fun clearFocus        (          )
    fun moveFocusForward  (          )
    fun moveFocusForward  (from: View)
    fun moveFocusBackward (          )
    fun moveFocusBackward (from: View)
    fun moveFocusUpward   (          )
    fun moveFocusUpward   (from: View)
    fun moveFocusDownward (          )
    fun moveFocusDownward (from: View)
    fun moveFocusToDefault(          )
}

interface FocusCycleRoot {
    val children: List<View>
}