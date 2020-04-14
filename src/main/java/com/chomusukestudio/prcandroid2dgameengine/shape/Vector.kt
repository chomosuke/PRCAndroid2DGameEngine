package com.chomusukestudio.prcandroid2dgameengine.shape

import com.chomusukestudio.prcandroid2dgameengine.square
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Vector(val x: Float, val y: Float) {
    fun rotateVector(centerOfRotation: Vector, angle: Float): Vector {
        if (angle == 0f)
        // yeah, I do zero angle a lot
            return this

        // translate point back to origin:
        var result = this - centerOfRotation

        // rotate point around origin
        result = result.rotateVector(angle)

        // translate point back:
        return result + centerOfRotation
    }

    fun rotateVector(angle: Float): Vector {
        val sinAngle = sin(angle)
        val cosAngle = cos(angle)
        val xNew = x * cosAngle - y * sinAngle
        val yNew = x * sinAngle + y * cosAngle
        return Vector(xNew, yNew)
    }

    val direction get() = atan2(y, x)
    val abs get() = sqrt(square(x) + square(y))
    val unitVector get() = this / this.abs

    fun scale(pivot: Vector, factor: Float) = Vector(pivot.x + factor * (x - pivot.x), pivot.y + factor * (y - pivot.y))
    fun scaleXY(factor: Vector) = Vector(x * factor.x, y * factor.y)
    fun offset(dx: Float, dy: Float) = Vector(x + dx, y + dy)
    fun mirrorXAxis() = Vector(x, -y)
    fun mirrorYAxis() = Vector(-x, y)
    fun swapXY() = Vector(y, x)

    operator fun plus(anotherVector: Vector) = Vector(x + anotherVector.x, y + anotherVector.y)
    operator fun minus(anotherVector: Vector) = Vector(x - anotherVector.x, y - anotherVector.y)
    operator fun times(anotherVector: Vector) = Vector(x * anotherVector.x, y * anotherVector.y)
    operator fun div(anotherVector: Vector) = Vector(x / anotherVector.x, y / anotherVector.y)

    operator fun unaryMinus() = Vector(-x, -y)

    operator fun times(factor: Float) = Vector(x * factor, y * factor)
    operator fun div(factor: Float) = Vector(x / factor, y / factor)

    override fun equals(other: Any?): Boolean {
        return if (other is Vector)
            x == other.x && y == other.y
        else false
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}