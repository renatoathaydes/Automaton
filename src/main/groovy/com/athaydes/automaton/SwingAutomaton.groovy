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

	SwingDragTo<SwingAutomaton> drag( Component component ) {
		def center = centerOf( component )
		new SwingDragTo( this, center.x, center.y )
	}

	static Point centerOf( Component component ) {
		def center = component.locationOnScreen
		center.x += component.width / 2
		center.y += component.height / 2
		return center
	}

}

class Swinger extends Automaton<Swinger> {

	protected Component component
	private delegate = SwingAutomaton.user

	static Swinger getUserWith( Component component ) {
		new Swinger( component: component )
	}

	Swinger clickOn( String selector, Speed speed = DEFAULT ) {
		delegate.clickOn( SwingUtil.lookup( selector, component ), speed )
		this
	}

	Swinger clickOn( Component component, Speed speed = DEFAULT ) {
		delegate.clickOn( component, speed )
		this
	}

	Swinger moveTo( String selector, Speed speed = DEFAULT ) {
		def component = SwingUtil.lookup( selector, component )
		delegate.moveTo( component, speed )
		this
	}

	Swinger moveTo( Component component, Speed speed = DEFAULT ) {
		delegate.moveTo( component, speed )
		this
	}

	SwingerDragTo drag( Component component ) {
		def center = SwingAutomaton.centerOf( component )
		new SwingerDragTo( this, center.x, center.y )
	}

	SwingerDragTo drag( String selector ) {
		def component = SwingUtil.lookup( selector, component )
		drag( component )
	}

}

class SwingDragTo<T extends Automaton<? extends Automaton>> extends DragTo<T> {

	protected SwingDragTo( T automaton, fromX, fromY ) {
		super( automaton, fromX, fromY )
	}

	T to( Component component, Speed speed = Automaton.DEFAULT ) {
		def center = SwingAutomaton.centerOf( component )
		to( center.x, center.y, speed )
	}

}

class SwingerDragTo extends SwingDragTo<Swinger> {

	protected SwingerDragTo( Swinger swinger, fromX, fromY ) {
		super( swinger, fromX, fromY )
	}

	Swinger to( String selector, Speed speed = Automaton.DEFAULT ) {
		def component = SwingUtil.lookup( selector, automaton.component )
		to( component, speed )
	}
}

