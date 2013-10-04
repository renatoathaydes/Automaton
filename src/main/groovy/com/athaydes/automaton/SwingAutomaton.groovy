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

	SwingAutomaton clickOn( Component component, Speed speed = DEFAULT ) {
		if ( !component ) return this
		def center = centerOf component
		moveTo( center.x as int, center.y as int, speed ).click()
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

class Swinger extends Automaton<Swinger> {

	Container container
	def delegate = SwingAutomaton.user

	static Swinger userWith( Container container ) {
		new Swinger( container: container )
	}

	Swinger clickOn( String selector, Speed speed = DEFAULT ) {
		delegate.clickOn( SwingUtil.lookup( selector, container ), speed )
		this
	}

	Swinger moveTo( String selector, Speed speed = DEFAULT ) {
		def component = SwingUtil.lookup( selector, container )
		delegate.moveTo( component, speed )
		this
	}

}

