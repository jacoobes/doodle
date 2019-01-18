package com.nectar.doodle.drawing.impl

import com.nectar.doodle.drawing.Renderer.Optimization
import com.nectar.doodle.drawing.Shadow
import com.nectar.doodle.geometry.Size
import org.w3c.dom.Node

/**
 * Created by Nicholas Eddy on 10/24/17.
 */

interface CanvasContext {
    var size          : Size
    var optimization  : Optimization
    val renderRegion  : Node
    var renderPosition: Node?
    val shadows       : List<Shadow>
}