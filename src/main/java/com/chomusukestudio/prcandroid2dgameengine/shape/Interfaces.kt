package com.chomusukestudio.prcandroid2dgameengine.shape

interface IMovable {
    fun move(displacement: Vector)
    fun rotate(centerOfRotation: Vector, angle: Float)
}

interface IOverlapable {
    val overlapper: Overlapper
}

interface ISolid: IMovable, IOverlapable

interface IRemovable {
    fun remove()
    val removed: Boolean
}