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
		[ 'name:': SwingUtil.&lookup, 'text:': SwingUtil.&text ].asImmutable()
	Component component
	protected delegate = SwingAutomaton.user
	Map<String, Closure<Component>> specialPrefixes

	protected Swinger( ) {}

	static Swinger getUserWith( Component component ) {
		new Swinger( specialPrefixes: DEFAULT_PREFIX_MAP, component: component )
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
		selector = ensurePrefixed selector
		delegate.clickOn( prefixed( selector ), speed )
		this
	}

	Swinger doubleClickOn( String selector, Speed speed = DEFAULT ) {
		selector = ensurePrefixed selector
		delegate.doubleClickOn( prefixed( selector ), speed )
		this
	}

	Swinger moveTo( String selector, Speed speed = DEFAULT ) {
		selector = ensurePrefixed selector
		delegate.moveTo( prefixed( selector ), speed )
		this
	}

	SwingerDragOn drag( String selector ) {
		selector = ensurePrefixed selector
		drag( prefixed( selector ) )
	}

	protected String ensurePrefixed( String selector ) {
		def prefixes = specialPrefixes.keySet()
		selector.size() > 5 && selector[ 0..4 ] in prefixes ?
			selector : "${prefixes[ 0 ]}${selector}"
	}

	protected prefixed( String selector ) {
		specialPrefixes[ selector[ 0..4 ] ]( selector[ 5..-1 ], component )
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
		def component = SwingUtil.lookup( selector, automaton.component )
		onto( component, speed )
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

