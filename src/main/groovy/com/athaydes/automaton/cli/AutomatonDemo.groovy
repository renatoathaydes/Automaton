package com.athaydes.automaton.cli

import com.athaydes.automaton.cli.demo.JavaFxDemo
import com.athaydes.automaton.cli.demo.SwingDemo

/**
 * @author Renato
 */
@Singleton
class AutomatonDemo {

	enum DemoOption {
		SWING, JAVAFX
	}

	void runDemo( String option ) {
		def demoOption = optionFor( option ?: 'swing' )
		if ( !demoOption ) return
		switch ( demoOption ) {
			case DemoOption.SWING: new SwingDemo().start()
				break
			case DemoOption.JAVAFX: new JavaFxDemo().start()
		}
	}

	DemoOption optionFor( String option ) {
		try {
			return DemoOption.valueOf( option.toUpperCase() )
		} catch ( IllegalArgumentException ignored ) {
			println "ERROR: Unknown demo option: $option"
			println "Valid options are ${DemoOption.values()*.name()*.toLowerCase()}"
			return null
		}
	}

}
