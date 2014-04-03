package com.athaydes.internal

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 *
 * User: Renato
 */
class TimeLimiter {

	private final executor = Executors.newSingleThreadExecutor( new ThreadFactory() {
		@Override
		Thread newThread( Runnable runnable ) {
			def thread = new Thread( runnable, 'automaton-timelimiter', )
			thread.daemon = true
			return thread
		}
	} )

	def abortAfter( Closure toRun, long timeToAbort, TimeUnit unit )
			throws TimeoutException {
		def result = null
		def exception = null
		Thread thread = null

		def future = executor.submit {
			thread = Thread.currentThread()
			try {
				result = toRun()
			} catch ( e ) {
				exception = e
				Thread.interrupted() // clear the interrupt flag
			}
		}

		try {
			future.get( timeToAbort, unit )
		} catch ( TimeoutException e ) {
			thread?.interrupt()
			throw e
		}

		if ( exception ) throw exception
		else result
	}

}