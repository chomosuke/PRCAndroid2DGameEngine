package com.chomusukestudio.prcandroid2dgameengine

import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.GLImage
import com.chomusukestudio.prcandroid2dgameengine.shape.*

class Image(resourceId: Int, vertex1: Vector, vertex2: Vector, vertex3: Vector, vertex4: Vector,
            overlapperVertexes: Array<Vector>?, showOverlapper: Boolean,
            z: Float, drawData: DrawData): ISolid, IRemovable {

    private val glImage = GLImage(resourceId, vertex1, vertex2, vertex3, vertex4, z, drawData)

    fun setColorSwap(colorBeSwapped: Color, colorSwappedTo: Color) {
        glImage.setColorSwap(colorBeSwapped.toArray(), colorSwappedTo.toArray())
    }

    var colorOffset: Color
        get() = Color(glImage.colorOffset[0],
                glImage.colorOffset[1],
                glImage.colorOffset[2],
                glImage.colorOffset[3])
        set(value) {
            glImage.colorOffset[0] = value.red
            glImage.colorOffset[1] = value.green
            glImage.colorOffset[2] = value.blue
            glImage.colorOffset[3] = value.alpha
        }

    var vertex4 = vertex4
        private set
    var vertex3 = vertex3
        private set
    var vertex2 = vertex2
        private set
    var vertex1 = vertex1
        private set

    override fun move(displacement: Vector) {
        glImage.move(displacement)
        (overlapper as PolygonalOverlapper).move(displacement)
        overlapperShape?.move(displacement)
        vertex1 += displacement
        vertex2 += displacement
        vertex3 += displacement
        vertex4 += displacement
    }

    override fun rotate(centerOfRotation: Vector, angle: Float) {
        glImage.rotate(centerOfRotation, angle)
        (overlapper as PolygonalOverlapper).rotate(centerOfRotation, angle)
        overlapperShape?.rotate(centerOfRotation, angle)
        vertex1 = vertex1.rotateVector(centerOfRotation, angle)
        vertex2 = vertex2.rotateVector(centerOfRotation, angle)
        vertex3 = vertex3.rotateVector(centerOfRotation, angle)
        vertex4 = vertex4.rotateVector(centerOfRotation, angle)
    }

    override val overlapper: Overlapper = if (overlapperVertexes != null) PolygonalOverlapper(overlapperVertexes) else EmptyOverlapper()

    private val overlapperShape = if (showOverlapper && overlapperVertexes != null)
        EarClipPolygonalShape(
                overlapperVertexes,
                Color(0f, 1f, 0f, 0.4f),
                BuildShapeAttr(-100f, true, drawData)
        )
    else null

    override fun remove() {
        glImage.remove()
        overlapperShape?.remove()
    }

    override val removed: Boolean
        get() = glImage.removed
}

private class PolygonalOverlapper(vertexes: Array<Vector>): Overlapper(), IMovable {
	private val trianglesVertexes = earClip(vertexes)

	override fun move(displacement: Vector) {
		for (i in trianglesVertexes.indices)
			for (j in trianglesVertexes[i].indices)
				trianglesVertexes[i][j] += displacement
	}

	override fun rotate(centerOfRotation: Vector, angle: Float) {
		for (i in trianglesVertexes.indices)
			for (j in trianglesVertexes[i].indices)
				trianglesVertexes[i][j] = trianglesVertexes[i][j].rotateVector(centerOfRotation, angle)
	}

	override val components: Array<Overlapper>
		get() = run {
			val triangularOverlappers = arrayOfNulls<TriangularOverlapper>(trianglesVertexes.size)
			for (i in trianglesVertexes.indices)
				triangularOverlappers[i] =
                        TriangularOverlapper(trianglesVertexes[i][0], trianglesVertexes[i][1], trianglesVertexes[i][2])
			return@run triangularOverlappers as Array<Overlapper>
		}
}