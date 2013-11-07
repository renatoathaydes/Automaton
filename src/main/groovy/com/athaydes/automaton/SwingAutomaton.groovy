package com.athaydes.automaton

import java.awt.*
import java.util.List

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
		moveTo( component, speed ).click()
	}

	SwingAutomaton doubleClickOn( Component component, Speed speed = DEFAULT ) {
		moveTo( component, speed ).doubleClick()
	}

	SwingAutomaton moveTo( Component component, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = centerOf component
		move( currPos, target, speed )
	}

	SwingDragOn<SwingAutomaton> drag( Component component ) {
		def center = centerOf( component )
		new SwingDragOn( this, center.x, center.y )
	}

	static Point centerOf( Component component ) {
		def center = component.locationOnScreen
		center.x += component.width / 2
		center.y += component.height / 2
		return center
	}

}

class Swinger extends Automaton<Swinger> {
	static final Map<String, Closure<Component>> DEFAULT_PREFIX_MAP =
		[
				'name:': SwingUtil.&lookup,
				'text:': SwingUtil.&text,
				'type:': SwingUtil.&type,
		].asImmutable()
	Component component
	protected delegate = SwingAutomaton.user
	Map<String, Closure<Component>> specialPrefixes

	protected Swinger( ) {}

	static Swinger getUserWith( Component component ) {
		new Swinger( specialPrefixes: DEFAULT_PREFIX_MAP, component: component )
	}

	Component get( String selector ) {
		findPrefixed( ensurePrefixed( selector ) ) as Component
	}

	Swinger clickOn( Component component, Speed speed = DEFAULT ) {
		delegate.clickOn( component, speed )
		this
	}

	Swinger doubleClickOn( Component component, Speed speed = DEFAULT ) {
		delegate.doubleClickOn( component, speed )
		this
	}

	Swinger moveTo( Component component, Speed speed = DEFAULT ) {
		delegate.moveTo( component, speed )
		this
	}

	SwingerDragOn drag( Component component ) {
		def center = SwingAutomaton.centerOf( component )
		new SwingerDragOn( this, center.x, center.y )
	}

	Swinger clickOn( String selector, Speed speed = DEFAULT ) {
		def prefix_selector = ensurePrefixed selector
		delegate.clickOn( findPrefixed( prefix_selector ), speed )
		this
	}

	Swinger doubleClickOn( String selector, Speed speed = DEFAULT ) {
		def prefix_selector = ensurePrefixed selector
		delegate.doubleClickOn( findPrefixed( prefix_selector ), speed )
		this
	}

	Swinger moveTo( String selector, Speed speed = DEFAULT ) {
		def prefix_selector = ensurePrefixed selector
		delegate.moveTo( findPrefixed( prefix_selector ), speed )
		this
	}

	SwingerDragOn drag( String selector ) {
		def prefix_selector = ensurePrefixed selector
		drag( findPrefixed( prefix_selector ) )
	}

	protected List ensurePrefixed( String selector ) {
		def prefixes = specialPrefixes.keySet()
		def prefix = prefixes.find { selector.startsWith it }
		[ prefix ?: prefixes[ 0 ], prefix ? selector - prefix : selector ]
	}

	protected findPrefixed( String prefix, String selector ) {
		def target = specialPrefixes[ prefix ]( selector, component )
		if ( target ) target else
			throw new RuntimeException( "Unable to locate prefix=$prefix, selector=$selector" )
	}

	/**
	 * @return Swinger whose root element is the first Window that can be found
	 * by calling {@code java.awt.Window.getWindows ( )}
	 */
	static Swinger forSwingWindow( ) {
		if ( Window.windows ) {
			getUserWith( Window.windows[ 0 ] )
		} else {
			throw new RuntimeException( 'Impossible to get any Swing window' )
		}
	}
}

class SwingDragOn<T extends Automaton<? extends Automaton>> extends DragOn<T> {

	protected SwingDragOn( T automaton, fromX, fromY ) {
		super( automaton, fromX, fromY )
	}

	T onto( Component component, Speed speed = Automaton.DEFAULT ) {
		def center = SwingAutomaton.centerOf( component )
		onto( center.x, center.y, speed )
	}

}

class SwingerDragOn extends SwingDragOn<Swinger> {

	protected SwingerDragOn( Swinger swinger, fromX, fromY ) {
		super( swinger, fromX, fromY )
	}

	Swinger onto( String selector, Speed speed = Automaton.DEFAULT ) {
		def prefix_selector = automaton.ensurePrefixed selector
		onto( automaton.findPrefixed( prefix_selector ), speed )
	}
}

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

