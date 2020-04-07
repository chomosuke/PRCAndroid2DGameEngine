package com.chomusukestudio.prcandroid2dgameengine.shape

import java.lang.Math.PI

class FullRingShape(center: Vector, a: Float, b: Float, innerPercentage: Float, color: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
    override var componentShapes: Array<Shape> = arrayOf(
        TopHalfRingShape(center, a, b, innerPercentage, color, buildShapeAttr),
            TopHalfRingShape(center, a, b, innerPercentage, color, buildShapeAttr)
    )
    
    init {
        componentShapes[1].rotate(center, PI.toFloat())
    }
}
