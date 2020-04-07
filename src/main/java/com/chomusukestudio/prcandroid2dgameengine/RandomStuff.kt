package com.chomusukestudio.prcandroid2dgameengine

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.SystemClock
import android.util.Log
import java.util.logging.Level
import java.util.logging.Logger
import com.chomusukestudio.prcandroid2dgameengine.shape.Vector
import java.lang.Math.random
import kotlin.math.sqrt

class PauseableTimer {
    @Volatile var paused = false
        set(value) {
            if (field && !value) { // paused to unpaused
                lastUptimeMillis = SystemClock.uptimeMillis()
                Log.d("upTimeMillisFromResume", "" + time)
                field = false
            } else if (!field && value) { // unpaused to paused
                Log.d("upTimeMillisFromPause", "" + time)
                field = true
            }
        }
    @Volatile var rate = 1.0
    @Volatile private var lastUptimeMillis = SystemClock.uptimeMillis()
    @Volatile private var time = 0.0
        private set
        get() {
            if (!paused) {
                field += (SystemClock.uptimeMillis() - lastUptimeMillis) * rate
                lastUptimeMillis = SystemClock.uptimeMillis()
            }
            return field
        }

    fun timeMillis() = time.toLong()
}

fun <R>runWithExceptionChecked(runnable: () -> R): R {
    try {
        return runnable()
    } catch (e: Exception) {
        val logger = Logger.getAnonymousLogger()
        logger.log(Level.SEVERE, "an exception was thrown in nextFrameThread", e)
        Log.e("exception", "in processingThread" + e)
        throw e
    }
}

fun scanForActivity(cont: Context): Activity {
    if (cont is Activity)
        return cont
    else if (cont is ContextWrapper)
        return scanForActivity(cont.baseContext)
    throw java.lang.Exception("I have no idea what is happening")
}

fun randFloat(b1: Float, b2: Float) = random().toFloat() * (b2 - b1) + b1

fun square(input: Double) = input * input
fun square(input: Float) = input * input
fun distance(p1: Vector, p2: Vector) = sqrt(square(p1.x - p2.x) + square(p1.y - p2.y))