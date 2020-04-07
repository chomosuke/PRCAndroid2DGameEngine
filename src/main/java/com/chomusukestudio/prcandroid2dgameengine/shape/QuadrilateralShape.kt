package com.chomusukestudio.prcandroid2dgameengine.shape

/**
 * Created by Shuang Li on 3/03/2018.
 */

class QuadrilateralShape// in order:
(vertex1: Vector, vertex2: Vector, vertex3: Vector, vertex4: Vector, color: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
    override var componentShapes: Array<Shape> = arrayOf(
        TriangularShape(vertex1, vertex2, vertex3, color, buildShapeAttr),
            TriangularShape(vertex1, vertex4, vertex3, color, buildShapeAttr)
    )

    fun setQuadrilateralShapeCoords(vertex1: Vector, vertex2: Vector, vertex3: Vector, vertex4: Vector) { // in order:
        (componentShapes[0] as TriangularShape).setTriangleCoords(vertex1, vertex2, vertex3)
        (componentShapes[1] as TriangularShape).setTriangleCoords(vertex1, vertex4, vertex3)
    }

    val vertex1 get() = (componentShapes[0] as TriangularShape).vertex1
    val vertex2 get() = (componentShapes[0] as TriangularShape).vertex2
    val vertex3 get() = (componentShapes[0] as TriangularShape).vertex3
    val vertex4 get() = (componentShapes[1] as TriangularShape).vertex2
}
