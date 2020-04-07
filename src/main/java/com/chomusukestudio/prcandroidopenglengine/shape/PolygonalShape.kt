package com.chomusukestudio.prcandroidopenglengine.shape

import kotlin.collections.ArrayList
import kotlin.math.PI

class PolygonalShape(vertexes: Array<Vector>, color: Color, buildShapeAttr: BuildShapeAttr): Shape() {
	override var componentShapes = Array<Shape>(vertexes.size - 2) {
		TriangularShape(vertexes[0], vertexes[it + 1], vertexes[it + 2], color, buildShapeAttr)
	}
	
	fun getVertex(index: Int): Vector {
		return when (index) {
			0 -> (componentShapes[0] as TriangularShape).vertex1
			1 -> (componentShapes[0] as TriangularShape).vertex2
			else -> (componentShapes[index - 2] as TriangularShape).vertex3
		}
	}
}

class EarClipPolygonalShape(private val vertexes: Array<Vector>, color: Color, buildShapeAttr: BuildShapeAttr): Shape() {
	override var componentShapes = run {
		val vertexeses = earClip(vertexes)
		Array<Shape>(vertexeses.size) {
			TriangularShape(vertexeses[it][0], vertexeses[it][1], vertexeses[it][2], color, buildShapeAttr)
		}
	}
	
	override fun move(displacement: Vector) {
		super.move(displacement)
		totalDisplacement += displacement
	}

	override fun rotate(centerOfRotation: Vector, angle: Float) {
		super.rotate(centerOfRotation, angle)
		totalRotation += angle
		totalDisplacement = totalDisplacement.rotateVector(centerOfRotation, angle)
	}
	private var totalDisplacement = Vector(0f, 0f)
	private var totalRotation = 0f
	fun getVertex(index: Int): Vector {
		return (vertexes[index] + totalDisplacement).rotateVector(totalDisplacement, totalRotation)
	}
}

// Triangulate a polygon with Ear clipping method
fun earClip(vertexes: Array<Vector>): Array<Array<Vector>> {
	val trianglesVertexes = arrayOfNulls<Array<Vector>>(vertexes.size - 2)
	var iT = 0
	val vertexes = vertexes.toCollection(ArrayList())
	while (vertexes.size > 2) {
		var iV = 1
		
		// find an inner angle smaller than 180 degree
		while (true) {
			
			// if IndexOutOfBounds means wrong order of vertexes
			val angle1 = (vertexes[iV] - vertexes[iV - 1]).direction
			val angle2 = (vertexes[iV + 1] - vertexes[iV]).direction
			if ((angle1 > 0 && (angle1 >= angle2 && angle1-PI <= angle2)) ||
					(angle1 <= 0 && (angle1 >= angle2 || angle1+PI <= angle2))) {
				// angle vertexes[i-1]vertexes[i]vertexes[i+1] anti clockwise is smaller than 180 degree
				// if made into a triangle it won't be outside the polygon
				
				val otherVertexes = vertexes.clone() as ArrayList<Vector>
				otherVertexes.removeAll(arrayOf(vertexes[iV-1], vertexes[iV], vertexes[iV+1]))
				if (!contain(TriangularOverlapper(vertexes[iV-1], vertexes[iV], vertexes[iV+1]), otherVertexes)) {
					// triangle also doesn't contain any other vertexes
					break
				}
			}
			iV++ // see if the next triangle meets requirement
		}
		
		// ear can be clipped
		trianglesVertexes[iT++] = arrayOf(vertexes[iV-1], vertexes[iV], vertexes[iV+1])
		vertexes.removeAt(iV)
	}
	return trianglesVertexes as Array<Array<Vector>>
}
private fun contain(triangularOverlapper: TriangularOverlapper, points: Collection<Vector>): Boolean {
	for (point in points)
		if (triangularOverlapper overlap PointOverlapper(point))
			return true
	return false
}

