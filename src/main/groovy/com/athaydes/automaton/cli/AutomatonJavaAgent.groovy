package com.athaydes.automaton.cli

import java.awt.*
import java.lang.instrument.Instrumentation

/**
 * @author Renato
 */
class AutomatonJavaAgent {

	static void premain( String agentArgs, Instrumentation instrumentation ) {
		if ( agentArgs ) {
			def scriptFile = new File( agentArgs )
			if ( scriptFile.exists() ) {
				waitForWindows {
					if ( Window.windows ) {
						sleep 2_000
						AutomatonScriptRunner.instance.runFile( scriptFile.absolutePath )
					} else {
						println "AutomatonJavaAgent: no Swing window detected"
					}
				}
			} else {
				println "AutomatonJavaAgent: will not start because file '$scriptFile' does not exist"
			}
		}
	}

	static void waitForWindows( Closure then ) {
		final CYCLE = 1_000 // ms
		final MAX_CYCLES = 30 // cycles

		def cycles = 0

		Thread.start {
			while ( !Window.windows && cycles < MAX_CYCLES ) {
				sleep CYCLE
				cycles++
			}
			then.run()
		}
	}

}
