package com.athaydes.automaton

import com.athaydes.automaton.selector.*
import com.athaydes.automaton.swing.selectors.SwingNavigator

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

	SwingAutomaton clickOn( Collection<? extends Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		components.each { c -> clickOn( c, speed ).pause( pauseBetween ) }
		this
	}

	SwingAutomaton doubleClickOn( Component component, Speed speed = DEFAULT ) {
		moveTo( component, speed ).doubleClick()
	}

	SwingAutomaton doubleClickOn( Collection<? extends Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		components.each { c -> doubleClickOn( c, speed ).pause( pauseBetween ) }
		this
	}

	SwingAutomaton moveTo( Component component, Speed speed = DEFAULT ) {
		moveTo( { centerOf( component ) }, speed )
	}

	SwingAutomaton moveTo( Collection<? extends Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
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
			throw new GuiItemNotFound( "Component not showing on screen: " + component )
		}
	}

}

class Swinger extends HasSelectors<Component, Swinger> {

	static final Map<String, AutomatonSelector<Component>> DEFAULT_SELECTORS =
			[
					'name:': SwingerSelectors.byName(),
					'text:': SwingerSelectors.byText(),
					'type:': SwingerSelectors.byType(),
			].asImmutable()

	Component root
	protected automaton = SwingAutomaton.user

	/**
	 * Gets a new instance of <code>Swinger</code> using the given
	 * top-level component.
	 * <br/>
	 * The search space is limited to the given Component.
	 * @param component top level Swing component to use
	 * @return a new Swinger instance
	 */
	static Swinger getUserWith( Component component ) {
		new Swinger( selectors: DEFAULT_SELECTORS, root: component )
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
			throw new GuiItemNotFound( 'Impossible to get any Swing window which is a JFrame' )
		}
	}

	protected Swinger() {}

	Component getAt( ComplexSelector selector ) {
		def res = doGetAt( selector, 1 )
		if ( res ) res.first()
		else throw new GuiItemNotFound( "Could not locate ${selector}" )
	}

	List<Component> getAll( ComplexSelector selector, int limit = Integer.MAX_VALUE ) {
		doGetAt( selector, limit )
	}

	Swinger clickOn( Component component, Speed speed = DEFAULT ) {
		automaton.clickOn( component, speed )
		this
	}

	Swinger clickOn( Collection<? extends Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
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

	Swinger doubleClickOn( Collection<? extends Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
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

	Swinger moveTo( Collection<? extends Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		automaton.moveTo( components, pauseBetween, speed )
		this
	}

	Swinger moveTo( String selector, Speed speed = DEFAULT ) {
		automaton.moveTo( this[ selector ], speed )
		this
	}

    /**
     * Enters the given text into the currently selected component.
     * @param text
     * @return this
     */
    Swinger enterText( String text ) {
        sleep 350 // Swing needs time to change focus!
        SwingUtilities.invokeAndWait {
            def focusOwner = getFocusedComponent()
            if ( focusOwner ) {
                try {
                    focusOwner.text = text
                } catch ( e ) {
                    throw new RuntimeException( 'Cannot enter text on the currently focused Component' )
                }

            } else {
                throw new GuiItemNotFound( 'Could not find the currently focused Component' )
            }
        }
        this
    }

    SwingerDragOn drag( Component component ) {
		def center = SwingAutomaton.centerOf( component )
		new SwingerDragOn( this, center.x, center.y )
	}

	SwingerDragOn drag( String selector ) {
		drag( this[ selector ] )
	}

	SwingerDragOn drag( Class<? extends Component> selector ) {
		drag( this[ selector ] )
	}

	SwingerDragOn drag( ComplexSelector selector ) {
		drag( this[ selector ] )
	}

    Component getFocusedComponent() {
        if ( root instanceof JFrame ) {
            return ( root as JFrame ).getFocusOwner()
        }
        def owner = null
        SwingNavigator.navigateBreadthFirst( root ) { JComponent c ->
            if( ReflectionHelper.callMethodIfExists( c, 'isFocusOwner' ) ) {
                owner = c
                return true
            }
        }
        owner
    }

    protected List<Component> doGetAt( ComplexSelector selector, int limit = Integer.MAX_VALUE ) {
		def prefixes_queries = selector.queries.collect { ensurePrefixed( it ) }
		def toMapEntries = { String prefix, String query ->
			new MapEntry( selectors[ prefix ], query )
		}

		def selectors_queries = prefixes_queries.collect( toMapEntries )

		def swingerSelector = entries2SwingerSelector( selector.matchType, selectors_queries )
		swingerSelector.apply( null, null, root, limit )
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
		onto( automaton[ selector ], speed )
	}

	Swinger onto( Class<? extends Component> selector, Speed speed = Automaton.DEFAULT ) {
		onto( automaton[ selector ], speed )
	}

	Swinger onto( ComplexSelector selector, Speed speed = Automaton.DEFAULT ) {
		onto( automaton[ selector ], speed )
	}

}


