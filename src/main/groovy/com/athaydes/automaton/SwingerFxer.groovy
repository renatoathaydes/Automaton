package com.athaydes.automaton

import com.athaydes.automaton.selector.AutomatonSelector
import com.athaydes.automaton.selector.ComplexSelector
import javafx.scene.Node

import java.awt.*

/**
 *
 * User: Renato
 */
class SwingerFxer extends Automaton<SwingerFxer> {

	final FXer fxer
	final Swinger swinger

	/**
	 * Gets a new instance of <code>SwingerFxer</code> using the given
	 * top-level component and node.
	 * <br/>
	 * The search space is limited to the given Component (in Swing) and
	 * Node (in JavaFX).
	 * @param component top level Swing container to use
	 * @param node top level JavaFX Node to use
	 * @return a new SwingerFxer instance
	 */
	static SwingerFxer getUserWith( Component component, Node node ) {
		new SwingerFxer( component, node )
	}

	protected SwingerFxer( Component component, Node node ) {
		swinger = Swinger.getUserWith( component )
		fxer = FXer.getUserWith( node )
	}

	/**
	 * Block until all events in the JavaFX Thread have been processed.
	 */
	SwingerFxer waitForFxEvents() {
		fxer.waitForFxEvents()
		this
	}

	void setJavaFxSelectors( Map<String, AutomatonSelector<Node>> selectors ) {
		fxer.selectors = selectors
	}

	void setSwingSelectors( Map<String, AutomatonSelector<Component>> selectors ) {
		swinger.selectors = selectors
	}

	def getAt( String selector ) {
		def nodes = fxer.getAll( selector, 1 )
		if ( nodes ) return nodes[ 0 ]
		swinger[ selector ]
	}

	def <K> K getAt( Class<K> type ) {
		if ( Node.isAssignableFrom( type ) ) fxer[ type ]
		else swinger[ type ]
	}

	def getAt( ComplexSelector selector ) {
		def nodes = fxer.getAll( selector, 1 )
		if ( nodes ) return nodes[ 0 ]
		swinger[ selector ]
	}

	Collection getAll( String selector ) {
		def nodes = fxer.getAll( selector )
		def components = swinger.getAll( selector )
		nodes + components
	}

	Collection getAll( Class type ) {
		if ( Node.isAssignableFrom( type ) )
			fxer.getAll( type )
		else
			swinger.getAll( type )
	}

	SwingerFxer clickOn( Node node, Speed speed = DEFAULT ) {
		fxer.clickOn( node, speed )
		this
	}

	SwingerFxer clickOnNodes( Collection<? extends Node> nodes, long pauseBetween = 100, Speed speed = DEFAULT ) {
		fxer.clickOnNodes( nodes, pauseBetween, speed )
		this
	}

	SwingerFxer clickOn( Component component, Speed speed = DEFAULT ) {
		swinger.clickOn( component, speed )
		this
	}

	SwingerFxer clickOn( Collection<? extends Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		swinger.clickOn( components, pauseBetween, speed )
		this
	}

	SwingerFxer clickOn( String selector, Speed speed = DEFAULT ) {
		clickOn( this[ selector ], speed )
		this
	}

	SwingerFxer clickOn( Class cls, Speed speed = DEFAULT ) {
		clickOn( this[ cls ], speed )
		this
	}

	SwingerFxer doubleClickOn( Node node, Speed speed = DEFAULT ) {
		fxer.doubleClickOn( node, speed )
		this
	}

	SwingerFxer doubleClickOnNodes( Collection<? extends Node> nodes, long pauseBetween = 100, Speed speed = DEFAULT ) {
		fxer.doubleClickOnNodes( nodes, pauseBetween, speed )
		this
	}

	SwingerFxer doubleClickOn( Component component, Speed speed = DEFAULT ) {
		swinger.doubleClickOn( component, speed )
		this
	}

	SwingerFxer doubleClickOn( Collection<? extends Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		swinger.doubleClickOn( components, pauseBetween, speed )
		this
	}

	SwingerFxer doubleClickOn( String selector, Speed speed = DEFAULT ) {
		doubleClickOn( this[ selector ], speed )
		this
	}

	SwingerFxer doubleClickOn( Class cls, Speed speed = DEFAULT ) {
		doubleClickOn( this[ cls ], speed )
		this
	}

	SwingerFxer doubleClickOn( ComplexSelector selector, Speed speed = DEFAULT ) {
		doubleClickOn( this[ selector ], speed )
		this
	}

	SwingerFxer moveTo( Node node, Speed speed = DEFAULT ) {
		fxer.moveTo( node, speed )
		this
	}

	SwingerFxer moveToNodes( Collection<? extends Node> nodes, long pauseBetween = 100, Speed speed = DEFAULT ) {
		fxer.moveToNodes( nodes, pauseBetween, speed )
		this
	}

	SwingerFxer moveTo( Component component, Speed speed = DEFAULT ) {
		swinger.moveTo( component, speed )
		this
	}

	SwingerFxer moveTo( Collection<? extends Component> components, long pauseBetween = 100, Speed speed = DEFAULT ) {
		swinger.moveTo( components, pauseBetween, speed )
		this
	}

	SwingerFxer moveTo( String selector, Speed speed = DEFAULT ) {
		moveTo( this[ selector ], speed )
		this
	}

	SwingerFxer moveTo( Class cls, Speed speed = DEFAULT ) {
		moveTo( this[ cls ], speed )
		this
	}

	SwingerFxer moveTo( ComplexSelector selector, Speed speed = DEFAULT ) {
		moveTo( this[ selector ], speed )
		this
	}

    SwingerFxer enterText( String text ) {
        swinger.enterText( text )
        this
    }

    Component getFocusedComponent() {
        swinger.focusedComponent
    }

	SwingerFXerDragOn drag( Node node ) {
		def target = centerOf node
		new SwingerFXerDragOn( this, target.x, target.y )
	}

	SwingerFXerDragOn drag( Component component ) {
		def target = centerOf component
		new SwingerFXerDragOn( this, target.x, target.y )
	}

	SwingerFXerDragOn drag( String selector ) {
		drag( this[ selector ] )
	}

	SwingerFXerDragOn drag( Class cls ) {
		drag( this[ cls ] )
	}

	SwingerFXerDragOn drag( ComplexSelector selector ) {
		drag( this[ selector ] )
	}

	Point centerOf( Node node ) {
		fxer.centerOf( node )
	}

	Point centerOf( Component component ) {
		SwingAutomaton.centerOf( component )
	}

}

class SwingerFXerDragOn extends DragOn<SwingerFxer> {

	protected SwingerFXerDragOn( SwingerFxer automaton, fromX, fromY ) {
		super( automaton, fromX, fromY )
	}

	SwingerFxer onto( Node node, Speed speed = Automaton.DEFAULT ) {
		new FXerDragOn( automaton.fxer, fromX, fromY )
				.onto( node, speed )
		automaton
	}

	SwingerFxer onto( Component component, Speed speed = Automaton.DEFAULT ) {
		new SwingerDragOn( automaton.swinger, fromX, fromY )
				.onto( component, speed )
		automaton
	}

	SwingerFxer onto( String selector, Speed speed = Automaton.DEFAULT ) {
		onto( automaton[ selector ], speed )
	}

	SwingerFxer onto( Class cls, Speed speed = Automaton.DEFAULT ) {
		onto( automaton[ cls ], speed )
	}

	SwingerFxer onto( ComplexSelector selector, Speed speed = Automaton.DEFAULT ) {
		onto( automaton[ selector ], speed )
	}

}

