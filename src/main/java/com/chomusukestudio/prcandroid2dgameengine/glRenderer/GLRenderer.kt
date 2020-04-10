package com.chomusukestudio.prcandroid2dgameengine.glRenderer

/**
 * Created by Shuang Li on 11/03/2018.
 */

import android.opengl.GLSurfaceView
import android.util.Log

import javax.microedition.khronos.opengles.GL10

import android.content.ContentValues.TAG
import android.opengl.GLES30
import com.chomusukestudio.prcandroid2dgameengine.PRCGLSurfaceView
import com.chomusukestudio.prcandroid2dgameengine.PauseableTimer
import com.chomusukestudio.prcandroid2dgameengine.ProcessingThread

class GLRenderer(val processingThread: ProcessingThread, private val PRCGLSurfaceView: PRCGLSurfaceView) : GLSurfaceView.Renderer {

    private val timer = PauseableTimer()
    fun setTimerRate(rate: Double) {
        timer.rate = rate
    }

    override fun onSurfaceCreated(unused: GL10, config: javax.microedition.khronos.egl.EGLConfig) {
        //enable transparency
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glEnable(GLES30.GL_BLEND)

        // Set the background frame color
        GLES30.glClearColor(0f, 0f, 0f, 1f)

        ShapeLayer.createGLProgram()
        TextureLayer.createGLProgram()
        Log.i(TAG, "onSurfaceCreated() called")

    }

    override fun onDrawFrame(unused: GL10) {
        if (!timer.paused) { // if this is called when paused for some reason don't do anything as nothing suppose to change

            processingThread.waitForLastFrame()

            // can't refresh buffers when processingThread is running or when drawing all triangles
            processingThread.layers.passArraysToBuffers()

            processingThread.internalGenerateNextFrame(timer.timeMillis())
        }
        // Clear the screen
        //        GLES30.glClear(GL_DEPTH_BUFFER_BIT);

        // Redraw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        // this is required on certain devices

        // Draw all!
        processingThread.layers.drawAll()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        // for transformation to matrix

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method

        Layer.refreshMatrix(
            arrayOf(processingThread.leftEdge, processingThread.rightEdge, processingThread.bottomEdge, processingThread.topEdge)
        )
    }

    fun pauseGLRenderer() {
        if (!timer.paused) {
            PRCGLSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            timer.paused = true
        }
    }
    fun resumeGLRenderer() {
        if (timer.paused) {
            // pausedTime have to be set before changing renderMode as change of renderMode will trigger
            // onDrawFrame which will cause timeMillis to be accessed before pauseTime being set
            PRCGLSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

            timer.paused = false
        }
    }
}