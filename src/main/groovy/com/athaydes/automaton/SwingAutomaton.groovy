package com.athaydes.automaton

import java.awt.*

/**
 *
 * User: Renato
 */
class SwingAutomaton extends Automaton<SwingAutomaton> {

	private static instance

	static synchronized SwingAutomaton getUser( ) {
		if ( !instance ) instance = new SwingAutomaton()
		instance
	}

	protected SwingAutomaton( ) {}

	SwingAutomaton clickOn( Component component ) {
		if ( !component ) return this
		def center = centerOf component
		moveTo( center.x as int, center.y as int ).click()
	}

	SwingAutomaton moveTo( Component component, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = centerOf component
		move( currPos, target, speed )
	}

	static Point centerOf( Component component ) {
		def center = component.locationOnScreen
		center.x += component.width / 2
		center.y += component.height / 2
		return center
	}

}

class Swinger extends SwingAutomaton {

	Container container

	static userWith( Container container ) {
		new Swinger( container: container )
	}

	Swinger clickOn( String selector ) {
		clickOn( SwingUtil.lookup( selector, container ) ) as Swinger
	}

	Swinger moveTo( String selector, Speed speed = DEFAULT ) {
		def component = SwingUtil.lookup( selector, container )
		moveTo( component, speed ) as Swinger
	}

}

