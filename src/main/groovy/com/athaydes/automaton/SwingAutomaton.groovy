package com.athaydes.automaton

import com.athaydes.automaton.selector.*

import javax.swing.*
import java.awt.*
import java.util.List

/**
 *
 * User: Renato
 */
class SwingAutomaton extends Automaton<SwingAutomaton> {

	private static instance

	/**
	 * Get the singleton instance of SwingAutomaton, which is lazily created.
	 * @return SwingAutomaton singleton instance
	 */
	static synchronized SwingAutomaton getUser() {
		if ( !instance ) instance = new SwingAutomaton()
		instance
	}

	protected SwingAutomaton() {}

	SwingAutomaton clickOn( Component component, Speed speed = DEFAULT ) {
		moveTo( component, speed ).click()
	}

	SwingAutomaton clickOn( Collection<Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		components.each { c -> clickOn( c, speed ).pause( pauseBetween ) }
		this
	}

	SwingAutomaton doubleClickOn( Component component, Speed speed = DEFAULT ) {
		moveTo( component, speed ).doubleClick()
	}

	SwingAutomaton doubleClickOn( Collection<Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		components.each { c -> doubleClickOn( c, speed ).pause( pauseBetween ) }
		this
	}

	SwingAutomaton moveTo( Component component, Speed speed = DEFAULT ) {
		moveTo( { centerOf( component ) }, speed )
	}

	SwingAutomaton moveTo( Collection<Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		components.each { c -> moveTo( c, speed ).pause( pauseBetween ) }
		this
	}

	SwingDragOn<SwingAutomaton> drag( Component component ) {
		def center = centerOf( component )
		new SwingDragOn( this, center.x, center.y )
	}

	static Point centerOf( Component component ) {
		assert component != null, 'Component could not be found'
		try {
			def center = component.locationOnScreen
			center.x += component.width / 2
			center.y += component.height / 2
			return center
		} catch ( IllegalComponentStateException ignore ) {
			throw new RuntimeException( "Component not showing on screen: " + component )
		}
	}

}

class Swinger extends Automaton<Swinger> {

	static final Map<String, ? extends SimpleSwingerSelector> DEFAULT_SELECTORS =
			[
					'name:': SwingerSelectors.byName(),
					'text:': SwingerSelectors.byText(),
					'type:': SwingerSelectors.byType(),
			].asImmutable()

	Component component
	protected automaton = SwingAutomaton.user
	Map<String, ? extends SimpleSwingerSelector> selectors

	/**
	 * Gets a new instance of <code>Swinger</code> using the given
	 * top-level component.
	 * <br/>
	 * The search space is limited to the given Component.
	 * @param component top level Swing component to use
	 * @return a new Swinger instance
	 */
	static Swinger getUserWith( Component component ) {
		new Swinger( selectors: DEFAULT_SELECTORS, component: component )
	}

	/**
	 * @return Swinger whose root element is the first Window that can be found
	 * by calling {@code java.awt.Window.getWindows ( )} which is an instance of
	 * {@code JFrame}.
	 */
	static Swinger forSwingWindow() {
		def isJFrame = { it instanceof JFrame }
		if ( Window.windows && Window.windows.any( isJFrame ) ) {
			getUserWith( Window.windows.find( isJFrame ) )
		} else {
			throw new RuntimeException( 'Impossible to get any Swing window which is a JFrame' )
		}
	}

	protected Swinger() {}

	Component getAt( String selector ) {
		findOnePrefixed( ensurePrefixed( selector ) )
	}

	def <K> K getAt( Class<K> type ) {
		findOnePrefixed( 'type:', type.name ) as K
	}

	Component getAt( ComplexSelector selector ) {
		def res = doGetAt( selector, 1 )
		if ( res ) res.first()
		else throw new RuntimeException( "Could not locate ${selector}" )
	}

	List<Component> getAll( String selector ) {
		findAllPrefixed ensurePrefixed( selector )
	}

	def <K> List<K> getAll( Class<K> cls ) {
		findAllPrefixed( "type:", cls.name ) as List<K>
	}

	List<Component> getAll( ComplexSelector selector ) {
		doGetAt( selector )
	}

	Swinger clickOn( Component component, Speed speed = DEFAULT ) {
		automaton.clickOn( component, speed )
		this
	}

	Swinger clickOn( Collection<Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		automaton.clickOn( components, pauseBetween, speed )
		this
	}

	Swinger clickOn( String selector, Speed speed = DEFAULT ) {
		automaton.clickOn( this[ selector ], speed )
		this
	}

	Swinger clickOn( ComplexSelector selector, Speed speed = DEFAULT ) {
		clickOn( this[ selector ], speed )
	}

	Swinger doubleClickOn( Component component, Speed speed = DEFAULT ) {
		automaton.doubleClickOn( component, speed )
		this
	}

	Swinger doubleClickOn( Collection<Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		automaton.doubleClickOn( components, pauseBetween, speed )
		this
	}

	Swinger doubleClickOn( String selector, Speed speed = DEFAULT ) {
		automaton.doubleClickOn( this[ selector ], speed )
		this
	}

	Swinger moveTo( Component component, Speed speed = DEFAULT ) {
		automaton.moveTo( component, speed )
		this
	}

	Swinger moveTo( Collection<Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		automaton.moveTo( components, pauseBetween, speed )
		this
	}

	Swinger moveTo( String selector, Speed speed = DEFAULT ) {
		automaton.moveTo( this[ selector ], speed )
		this
	}

	SwingerDragOn drag( Component component ) {
		def center = SwingAutomaton.centerOf( component )
		new SwingerDragOn( this, center.x, center.y )
	}

	SwingerDragOn drag( String selector ) {
		drag( this[ selector ] )
	}

	protected List ensurePrefixed( String selector ) {
		def prefixes = selectors.keySet()
		def prefix = prefixes.find { selector.startsWith it }
		[ prefix ?: prefixes[ 0 ], prefix ? selector - prefix : selector ]
	}

	protected Component findOnePrefixed( String prefix, String query ) {
		def target = findAllPrefixed( prefix, query, 1 )
		if ( target ) target.first() else
			throw new RuntimeException( "Could not locate prefix=$prefix, selector=$query" )
	}

	protected List<Component> findAllPrefixed( String prefix, String query, int limit = Integer.MAX_VALUE ) {
		SimpleSwingerSelector swingSelector = selectors[ prefix ]
		swingSelector.apply( query, component, limit )
	}

	protected List<Component> doGetAt( ComplexSelector selector, int limit = Integer.MAX_VALUE ) {
		def prefixes_queries = selector.queries.collect { ensurePrefixed( it ) }
		def toMapEntries = { String prefix, String query ->
			new MapEntry( selectors[ prefix ], query )
		}

		def selectors_queries = prefixes_queries.collect( toMapEntries )

		def swingerSelector = entries2SwingerSelector( selector.matchType, selectors_queries )
		swingerSelector.apply( null, component, limit )
	}

	private CompositeSwingerSelector entries2SwingerSelector( MatchType type, List<MapEntry> selectors_queries ) {
		switch ( type ) {
			case MatchType.ANY:
				return new UnionSwingerSelector( selectors_queries )
			case MatchType.ALL:
				return new IntersectSwingerSelector( selectors_queries )
			default:
				throw new RuntimeException( "Forgot to implement selector of type ${type}" )
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
		onto( automaton.findOnePrefixed( prefix_selector ), speed )
	}
}


