package com.chomusukestudio.prcandroidopenglengine

import android.app.Activity
import android.util.Log
import android.view.MotionEvent
import com.chomusukestudio.prcandroidopenglengine.glRenderer.Layers
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

abstract class ProcessingThread {
    val layers = Layers()
    
    abstract fun generateNextFrame(timeInMillis: Long)
    abstract fun onTouchEvent(e: MotionEvent): Boolean

    private val nextFrameThread = Executors.newSingleThreadExecutor { r -> Thread(r, "nextFrameThread") }
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    
    internal fun _generateNextFrame(timeInMillis: Long) {
            finished = false // haven't started
            nextFrameThread.submit {
                runWithExceptionChecked {

                    generateNextFrame(timeInMillis)

                    // finished
                    finished = true
                    // notify waitForLastFrame
                    lock.lock()
                    condition.signal() // wakes up GLThread
                    //                Log.v("Thread", "nextFrameThread notified lockObject");
                    lock.unlock()
                }
            }
    }

    internal fun waitForLastFrame() {
        // wait for the last nextFrameThread
        lock.lock()
        // synchronized outside the loop so other thread can't notify when it's not waiting
        while (!finished) {
            //                Log.v("Thread", "nextFrameThread wait() called");
            try {
                condition.await() // wait, last nextFrameThread will wake this Thread up
            } catch (e: InterruptedException) {
                Log.e("lock", "Who would interrupt lock? They don't even have the reference.", e)
            }

            //                Log.v("Thread", "nextFrameThread finished waiting");
        }
        lock.unlock()
    }

    @Volatile var finished = true // last frame that doesn't exist has finish
}