package com.athaydes.automaton.assertion

import com.athaydes.automaton.SwingUtil
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

/**
 * @author Renato
 */
class AutomatonMatcher {

	static TypeSafeMatcher hasText( final String text ) {
		def result = null
		[
				matchesSafely: { item ->
					result = SwingUtil.callMethodIfExists( item, "getText" )
					result == text
				},
				describeTo: { Description description ->
					description.appendText( "Expected text '$text' but " +
							( result == [ ] ?
								"object did not have a 'getText' method" :
								" found '$result'" ) )
				}
		] as TypeSafeMatcher
	}

}
