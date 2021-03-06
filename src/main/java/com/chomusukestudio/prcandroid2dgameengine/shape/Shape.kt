package com.chomusukestudio.prcandroid2dgameengine.shape

import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData


/**
 * Created by Shuang Li on 28/02/2018.
 */

abstract class Shape : ISolid, IRemovable {
    /* IMPORTANT
     inheriting note:
     subclass need to defined the constructor and other that might be worth providing
      */
    
    // TriangularShape is the end of all recursion.
    // hahahahaha it sounds so cool!
    
    abstract var componentShapes: Array<Shape>
        protected set
    
    open var shapeColor: Color
        get() = componentShapes[0].shapeColor
        set(value) {
            for (componentShape in componentShapes)
                componentShape.shapeColor = value
        }

    open var visibility: Boolean
        set(value) {
            if (value != visibility) {
                for (componentShape in componentShapes)
                    componentShape.visibility = value
            }
        }
        get() = componentShapes[0].visibility
    
    override val overlapper: Overlapper
        get() = object : ComponentOverlapper() {
            override val components: Array<Overlapper> = Array(componentShapes.size) { componentShapes[it].overlapper }
        }
    
    override fun move(displacement: Vector) {
        if (displacement.x == 0f && displacement.y == 0f) {
            return // yeah i do that a lot
        }
        for (componentShape in componentShapes)
            componentShape.move(displacement)
    }
    
    override fun rotate(centerOfRotation: Vector, angle: Float) {
        if (angle == 0f) {
            return  // as mind blowing as it is, people like me do zero angle a lot
        }
        // positive is counter clockwise
        for (componentShape in componentShapes)
            componentShape.rotate(centerOfRotation, angle)
    }
    
    open fun resetAlpha(alpha: Float) {
        for (componentShape in componentShapes) {
            componentShape.resetAlpha(alpha)
        }
    }
    
    open fun changeShapeColor(dRed: Float, dGreen: Float, dBlue: Float, dAlpha: Float) {
        for (componentShape in componentShapes)
            componentShape.changeShapeColor(dRed, dGreen, dBlue, dAlpha)
    }

    override var removed = false
        protected set
    override fun remove() {
        for (componentShape in componentShapes)
            componentShape.remove()
        removed = true
    }
}

data class Color (val red: Float, val green: Float, val blue: Float, val alpha: Float) {
    fun toArray() = arrayOf(red, green, blue, alpha)
}

data class BuildShapeAttr(val z: Float, val visibility: Boolean, val drawData: DrawData) { // never set this as a property
    fun newAttrWithChangedZ(dz: Float) = BuildShapeAttr(z + dz, visibility, drawData)
    fun newAttrWithNewVisibility(visibility: Boolean) = BuildShapeAttr(z, visibility, drawData)
}