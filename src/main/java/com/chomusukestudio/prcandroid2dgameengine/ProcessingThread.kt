package com.chomusukestudio.prcandroid2dgameengine

import android.util.Log
import android.view.MotionEvent
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

abstract class ProcessingThread {
    val drawData = DrawData()
    
    protected abstract fun generateNextFrame(timeInMillis: Long)
    open fun onTouchEvent(e: MotionEvent): Boolean = false
    protected abstract fun getLeftRightBottomTopBoundaries(width: Int, height: Int): FloatArray

    fun updateBoundaries(width: Int, height: Int) {
        drawData.leftRightBottomTopEnds = getLeftRightBottomTopBoundaries(width, height)
        drawData.setPixelSize(width, height)
    }

    private val nextFrameThread = Executors.newSingleThreadExecutor { r -> Thread(r, "nextFrameThread") }
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    
    internal fun internalGenerateNextFrame(timeInMillis: Long) {
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

    @Volatile private var finished = true // last frame that doesn't exist has finish
}