package com.chomusukestudio.prcandroid2dgameengine.shape

abstract class Overlapper {
    infix fun overlap(anotherOverlapper: Overlapper): Boolean {
        val thisOverlapOther = overlapToOverride(anotherOverlapper)
        if (thisOverlapOther != null)
            return thisOverlapOther
        val otherOverlapthis = anotherOverlapper.overlapToOverride(this)
        if (otherOverlapthis != null)
            return otherOverlapthis
        throw RuntimeException("the combination of overlappers are not implemented")
    }

    // return null if the combination is not implemented in this.
    protected open fun overlapToOverride(anotherOverlapper: Overlapper): Boolean? {
        return null
    }
}

abstract class ComponentOverlapper: Overlapper() {
    protected abstract val components: Array<Overlapper>

    final override fun overlapToOverride(anotherOverlapper: Overlapper): Boolean? {
        for (component in components)
            if (anotherOverlapper.overlap(component))
            // if this overlapper provides no solution, pass itself or it's components to the other one
                return true
        return false
    }
}

class PointOverlapper(val point: Vector): Overlapper() {
    override fun equals(other: Any?): Boolean {
        return if (other is PointOverlapper)
            point == other.point
        else false
    }

    override fun hashCode(): Int {
        return point.hashCode()
    }
}

class LineSegmentOverlapper(val p1: Vector, val p2: Vector): Overlapper() {
    override fun overlapToOverride(anotherOverlapper: Overlapper): Boolean? {
        when (anotherOverlapper) {
            is LineSegmentOverlapper -> {
                val m1 = (p1.y - p2.y) / (p1.x - p2.x)
                val c1 = p1.y - (m1 * p1.x)
                val m2 = (anotherOverlapper.p1.y -
                        anotherOverlapper.p2.y) / (
                        anotherOverlapper.p1.x -
                                anotherOverlapper.p2.x)
                val c2 =
                        anotherOverlapper.p1.y - (m2 *
                                anotherOverlapper.p1.x)

                if (m1 == m2) {
                    return if (c1 == c2) {
                        // segment on same line, might overlap or not
                        isBetween(p1.x, p2.x, anotherOverlapper.p1.x) ||
                                isBetween(p1.x, p2.x, anotherOverlapper.p2.x) ||
                                // only possibility left is this line inside the other
                                isBetween(anotherOverlapper.p1.x, anotherOverlapper.p2.x, p1.x)
                    } else
                        false // means parallel, no intersection
                }
                val intersectionX = intersectionX(m1, c1, m2, c2)
                // intersection within both segment
                return isBetween(p1.x, p2.x, intersectionX) &&
                        isBetween(anotherOverlapper.p1.x, anotherOverlapper.p2.x, intersectionX)

            }
            else -> return null
        }
    }
    private fun intersectionX(m1: Float, c1: Float, m2: Float, c2: Float) = (c2 - c1) / (m1 - m2)
    private fun isBetween(x1: Float, x2: Float, x: Float) = (x in x1..x2) || (x in x2..x1)
}

class EmptyOverlapper() : Overlapper() {
    override fun overlapToOverride(anotherOverlapper: Overlapper): Boolean? {
        return false
    }
}