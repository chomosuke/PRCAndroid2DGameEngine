package com.chomusukestudio.prcandroid2dgameengine.glRenderer

import com.chomusukestudio.prcandroid2dgameengine.ParallelForI
import java.util.ArrayList
import java.util.concurrent.locks.ReentrantLock

class Layers : Iterable<Layer> { // a group of arrayList
    private val arrayList = ArrayList<Layer>()

    override fun iterator() = arrayList.iterator()

    fun remove(element: Layer) {
        lockOnArrayList.lock()
        arrayList.remove(element)
        lockOnArrayList.unlock()
    }

    fun insert(newLayer: Layer) {
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

//        fun offsetAllLayers(dOffsetX: Float, dOffsetY: Float) {
//            for (layer in arrayList)
//                layer.offsetLayer(dOffsetX, dOffsetY)
//        }

    private val lockOnArrayList = ReentrantLock()

    fun drawAll() {
        // no need to sort, already in order
        lockOnArrayList.lock() // for preventing concurrent modification
        for (layer in arrayList) { // draw arrayList in order
            layer.drawLayer()
        }
        lockOnArrayList.unlock()
    }

    private val parallelForIForPassArraysToBuffers = ParallelForI(20, "passArraysToBuffers")
    fun passArraysToBuffers() {
        lockOnArrayList.lock()

        parallelForIForPassArraysToBuffers.run({ i ->
            arrayList[i].passArraysToBuffers()
        }, arrayList.size)
        parallelForIForPassArraysToBuffers.waitForLastRun()

//        for (layer in arrayList)
//            layer.passArraysToBuffers()

        lockOnArrayList.unlock()
    }
}