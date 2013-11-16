package com.athaydes.automaton.selector

import java.awt.Component

/**
 * @author Renato
 */
abstract class SwingerSelector extends Closure<Component> {

	SwingerSelector( Object owner ) {
		super( owner )
	}

	@Override
	final Component call( Object... args ) {
		assert args.length == 2
		assert args[ 0 ] instanceof String
		assert args[ 1 ] instanceof Component
		call( args[ 0 ] as String, args[ 1 ] as Component )
	}

	abstract Component call( String selector, Component component )

}
