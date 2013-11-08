package com.athaydes.automaton.assertion

import com.athaydes.internal.matcher.GenericMatcher
import org.hamcrest.TypeSafeMatcher

/**
 * @author Renato
 */
class AutomatonMatcher {


	static TypeSafeMatcher hasText( final String text ) {
		new GenericMatcher( text, "getText" )
	}

	static TypeSafeMatcher hasValue( final value ) {
		new GenericMatcher( value, "getValue", "getText",
				"getSelected", "getColor" )
	}

	static TypeSafeMatcher selected( ) {
		new GenericMatcher( true, "isSelected" )
	}

	static TypeSafeMatcher visible( ) {
		new GenericMatcher( true, "isVisible" )
	}

	static TypeSafeMatcher showing( ) {
		new GenericMatcher( true, "isShowing" )
	}

}
