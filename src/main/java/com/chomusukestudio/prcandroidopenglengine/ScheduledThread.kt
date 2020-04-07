package com.chomusukestudio.projectrocketc.ThreadClasses

import android.os.SystemClock
import com.chomusukestudio.prcandroidopenglengine.runWithExceptionChecked
import java.util.concurrent.Executors

class ScheduledThread(private val periodInMillisecond: Long, private val task: () -> Unit) {
    private val name = "Scheduled Thread with period of $periodInMillisecond millisecond."
    @Volatile private var running = false
    private val singleThread = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, name) }
    fun run() {
        if (running) {
            throw IllegalThreadStateException("$name is running")
        } else {
            running = true
        }
        singleThread.submit {
            runWithExceptionChecked {
                while (running) {
                    val startTime = SystemClock.uptimeMillis()
                    task()
                    val timeTaken = SystemClock.uptimeMillis() - startTime
                    if (periodInMillisecond - timeTaken > 0)
                        Thread.sleep(periodInMillisecond - timeTaken)
                }
            }
        }
    }

    fun pause() {
        running = false
    }
}