package com.athaydes.automaton

import javafx.scene.Node

import java.awt.*

/**
 *
 * User: Renato
 */
class SwingerFxer extends Automaton<SwingerFxer> {

	final FXer fxer
	final Swinger swinger

	final fxSelector1stChars = [ '.', '#' ].asImmutable()

	static SwingerFxer userWith( Container container, Node node ) {
		new SwingerFxer( container, node )
	}

	protected SwingerFxer( Container container, Node node ) {
		swinger = Swinger.getUserWith( container )
		fxer = FXer.getUserWith( node )
	}

	SwingerFxer clickOn( Node node, Speed speed = DEFAULT ) {
		fxer.clickOn( node, speed )
		this
	}

	SwingerFxer clickOn( String selector, Speed speed = DEFAULT ) {
		if ( isJavaFXSelector( selector ) )
			fxer.clickOn( selector, speed )
		else
			swinger.clickOn( selector, speed )
		this
	}

	SwingerFxer moveTo( Node node, Speed speed = DEFAULT ) {
		fxer.moveTo( node, speed )
		this
	}

	SwingerFxer moveTo( String selector, Speed speed = DEFAULT ) {
		if ( isJavaFXSelector( selector ) )
			fxer.moveTo( selector, speed )
		else
			swinger.moveTo( selector, speed )
		this
	}

	Point centerOf( Node node ) {
		fxer.centerOf( node )
	}

	private boolean isJavaFXSelector( String selector ) {
		selector ? selector[ 0 ] in fxSelector1stChars : false
	}


}
