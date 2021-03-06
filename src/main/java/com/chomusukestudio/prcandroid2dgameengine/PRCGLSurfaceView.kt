package com.chomusukestudio.prcandroid2dgameengine

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.GLRenderer
import java.util.logging.Level
import java.util.logging.Logger
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

open class PRCGLSurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {

    init {
        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(3)
    }

    lateinit var mRenderer: GLRenderer

    internal inner class MyConfigChooser : EGLConfigChooser {// this class is for antialiasing

        override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig? {

            val attribs = intArrayOf(EGL10.EGL_LEVEL, 0, EGL10.EGL_RENDERABLE_TYPE, 4, // EGL_OPENGL_ES2_BIT
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER, EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8, EGL10.EGL_BLUE_SIZE, 8, EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_SAMPLE_BUFFERS, 1, EGL10.EGL_SAMPLES, 4, EGL10.EGL_NONE)// This is for 4x MSAA.

            val configs = arrayOfNulls<EGLConfig>(1)
            val configCounts = IntArray(1)
            egl.eglChooseConfig(display, attribs, configs, 1, configCounts)

            return if (configCounts[0] == 0) {
                // Failed! Error handling.
                null
            } else {
                configs[0]
            }
        }
    }

    fun initializeRenderer(processingThread: ProcessingThread) {

        setEGLConfigChooser(MyConfigChooser())// antialiasing

        mRenderer = GLRenderer(processingThread, this)

        // Set the Renderer for drawing on the GLSurfaceView
        debugFlags = DEBUG_LOG_GL_CALLS
        setRenderer(mRenderer)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        var nullPointerFailure: Boolean
        do {
            try {
                super.surfaceCreated(holder)
                nullPointerFailure = false
            } catch (e: NullPointerException) {
                // this means setRenderer() haven't being called yet (most likely)
                val logger = Logger.getAnonymousLogger()
                logger.log(Level.SEVERE, "an exception was thrown in nextFrameThread", e)
                // so log it and try again
                nullPointerFailure = true
            }
        } while (nullPointerFailure)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        return mRenderer.processingThread.onTouchEvent(e) // we know that the context is MainActivity
    }
}