package com.athaydes.internal

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 *
 * User: Renato
 */
class TimeLimiter {

    def executor = Executors.newSingleThreadExecutor()

    def abortAfter( Closure toRun, long timeToAbort, TimeUnit unit )
    throws TimeoutException {
        def latch = new CountDownLatch( 1 )
        def result = null
        def exception = null
        executor.submit {
            try { result = toRun() } catch ( e ) { exception = e }
            latch.countDown()
        }
        def isCompleted = latch.await( timeToAbort, unit )
        if ( !isCompleted ) {
            executor.shutdownNow()
            executor = Executors.newSingleThreadExecutor()
            throw new TimeoutException()
        } else {
            if ( exception ) throw exception
            else result
        }
    }

}