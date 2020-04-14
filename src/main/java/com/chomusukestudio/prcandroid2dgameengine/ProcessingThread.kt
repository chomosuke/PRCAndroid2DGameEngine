package com.chomusukestudio.prcandroid2dgameengine

import android.content.Context
import android.view.MotionEvent
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.DrawData
import com.chomusukestudio.prcandroid2dgameengine.threadClasses.ProcessWaiter
import java.util.concurrent.Executors

abstract class ProcessingThread(context: Context) {
    val drawData = DrawData(context)
    
    protected abstract fun generateNextFrame(timeInMillis: Long)
    open fun onTouchEvent(e: MotionEvent): Boolean = false
    protected abstract fun getLeftRightBottomTopBoundaries(width: Int, height: Int): FloatArray
    protected open fun initializeWithBoundaries() {}

    private val initializationWaiter = ProcessWaiter()
    fun waitForInit() = initializationWaiter.waitForFinish()

    private var boundariesUpdated = false
    fun updateBoundaries(width: Int, height: Int) {
        drawData.setLeftRightBottomTopEnds(getLeftRightBottomTopBoundaries(width, height))
        drawData.setPixelSize(width, height)
        if (!boundariesUpdated) {
            boundariesUpdated = true
            initializationWaiter.markAsStarted()
            initializeWithBoundaries()
            initializationWaiter.markAsFinished()
        }
    }

    private val nextFrameThread = Executors.newSingleThreadExecutor { r -> Thread(r, "nextFrameThread") }
    private val processWaiter = ProcessWaiter()
    
    internal fun internalGenerateNextFrame(timeInMillis: Long) {
        if (!pausedForChanges) { // if aren't pausing for changes
            processWaiter.markAsStarted()
            nextFrameThread.submit {
                runWithExceptionChecked {
                    generateNextFrame(timeInMillis)
                    processWaiter.markAsFinished()
                }
            }
        }
    }

    fun waitForLastFrame() = processWaiter.waitForFinish()

    /**
     * Note that this function will not return until last generateNextFrame() return.
     * Hence calling this in generateNextFrame() will cause a deadlock.
     */
    @Volatile private var pausedForChanges = false
    fun pause() {
        if (!pausedForChanges) {
            pausedForChanges = true
            waitForLastFrame()
        }
    }
    fun resume() {
        pausedForChanges = false
    }
}