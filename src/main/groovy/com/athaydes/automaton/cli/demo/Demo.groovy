package com.athaydes.automaton.cli.demo

import com.athaydes.automaton.cli.AutomatonScriptRunner

/**
 *
 */
abstract class Demo {

	void runScript( String text, def output, Closure onCompletion ) {
		def writer = [ write: { s -> output.append( s as String ) } ]
		Thread.start {
			AutomatonScriptRunner.instance.runScript( text, writer )
			onCompletion.run()
		}
	}

}
