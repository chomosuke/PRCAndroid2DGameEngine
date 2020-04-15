package com.chomusukestudio.prcandroid2dgameengine.threadClasses

import android.util.Log
import java.util.concurrent.locks.ReentrantLock

class ProcessWaiter {

    @Volatile private var finished = true // last frame that doesn't exist has finish

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    fun markAsStarted() {
        finished = false
    }

    fun markAsFinished() {
        // finished
        finished = true
        // notify
        lock.lock()
        condition.signal() // wakes up waitForFinish
        lock.unlock()
    }

    fun waitForFinish() {
        lock.lock()
        // synchronized outside the loop so other thread can't notify when it's not waiting
        while (!finished) {
            try {
                condition.await() // wait for markAsFinished() to wake this Thread up
            } catch (e: InterruptedException) {
                Log.e("lock", "Who would interrupt lock? They don't even have the reference.", e)
            }
        }
        lock.unlock()
    }
}