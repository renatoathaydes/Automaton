package com.athaydes.internal.matcher

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

/**
 * @author Renato
 */
class GenericMatcher extends TypeSafeMatcher {

	private static final METHOD_DOES_NOT_EXIST = new Object()
	def result
	final String[] methodNames
	final expected

	GenericMatcher( expected, String... methodNames ) {
		this.expected = expected
		this.methodNames = methodNames
	}

	@Override
	protected boolean matchesSafely( Object item ) {
		def methodName = methodNames.find { method ->
			item?.metaClass?.respondsTo( item, method )
		}
		if ( methodName )
			result = item."$methodName"()
		else
			result = METHOD_DOES_NOT_EXIST
		result == expected
	}

	@Override
	void describeTo( Description description ) {
		description.appendText( "Expected '$expected' but " +
				( result.is( METHOD_DOES_NOT_EXIST ) ?
					"could not find an appropriate method to call! " +
							"Are you sure you're using the right matcher?" :
					"found '$result'" ) )
	}

}
