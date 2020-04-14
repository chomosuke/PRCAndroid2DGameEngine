package com.chomusukestudio.prcandroid2dgameengine.glRenderer

import android.content.Context
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import com.chomusukestudio.prcandroid2dgameengine.threadClasses.ParallelForI
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs

class DrawData(private val context: Context) { // a group of arrayList
    private val arrayList = ArrayList<Layer>()

    private var lrbtEnds: FloatArray? = null
    private val leftRightBottomTopEnds: FloatArray
        inline get() = lrbtEnds ?: throw RuntimeException("Boundaries haven't been initialized")

    fun setLeftRightBottomTopEnds (value: FloatArray) {
            lrbtEnds = value
            for (layer in layers) {
                layer.setLRBTEnds(value)
            }
        }

    val leftEnd get() = leftRightBottomTopEnds[0]
    val rightEnd get() = leftRightBottomTopEnds[1]
    val bottomEnd get() = leftRightBottomTopEnds[2]
    val topEnd get() = leftRightBottomTopEnds[3]

    lateinit var pixelSize: Vector // for antialiasing
        private set
    fun setPixelSize(width: Int, height: Int) {
        pixelSize = Vector(abs(leftRightBottomTopEnds[0] - leftRightBottomTopEnds[1]) / width,
                abs(leftRightBottomTopEnds[2] - leftRightBottomTopEnds[3]) / height)
        for (layer in layers) {
            layer.pixelSize = pixelSize
        }
    }

    val layers get() = arrayList.iterator()

    fun remove(element: Layer) {
        lockOnArrayList.lock()
        arrayList.remove(element)
        lockOnArrayList.unlock()
    }

    fun reset() {
        lockOnArrayList.lock()
        arrayList.removeAll(arrayList)
        lockOnArrayList.unlock()
    }

    inline fun <reified T : Layer>getLayer(z: Float, layerFactory:() -> T): T {
        for (layer in layers) {
            if (layer.z == z && layer is T) {
                return layer // find the layer with that z
            }
        }

        // there is no layer with that z so create one and return index of that layer
        val newLayer = layerFactory()
        insert(newLayer)
        return newLayer
    }
    fun insert(newLayer: Layer) { // public as getLayer is public and inline

        if (lrbtEnds != null) {
            // set newLayer's varies properties to Layers'
            newLayer.setLRBTEnds(lrbtEnds!!)
            newLayer.pixelSize = pixelSize
        }
        newLayer.context = context

        var i = 0
        while (true) {
            if (i == arrayList.size) {
                // already the last one
                lockOnArrayList.lock()
                arrayList.add(newLayer)
                lockOnArrayList.unlock()
                break
            }
            if (newLayer.z > arrayList[i].z) {
                // if the new z is just bigger than this z
                // put it before this layer
                lockOnArrayList.lock()
                arrayList.add(i, newLayer)
                lockOnArrayList.unlock()
                break
            }
            i++
        }
    }

    private val lockOnArrayList = ReentrantLock()

    fun drawAll() {
        // no need to sort, already in order
        lockOnArrayList.lock() // for preventing concurrent modification
        for (layer in arrayList) { // draw arrayList in order
            layer.drawLayer()
        }
        lockOnArrayList.unlock()
    }

    private val parallelForIForPassArraysToBuffers =
        ParallelForI(
            20,
            "passArraysToBuffers"
        )
    fun passArraysToBuffers() {
        lockOnArrayList.lock()

        parallelForIForPassArraysToBuffers.run({ i ->
            arrayList[i].passArraysToBuffers()
        }, arrayList.size)
        parallelForIForPassArraysToBuffers.waitForLastRun()

        lockOnArrayList.unlock()
    }
}