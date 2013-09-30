package com.athaydes.internal

import java.awt.event.KeyEvent

import static java.awt.event.KeyEvent.*

/**
 *
 * User: Renato
 */
class RobotTypingUtil {

	static final keyMap = [
			'*': VK_MULTIPLY, '-': VK_MINUS, '+': VK_ADD,
			'/': VK_DIVIDE, ',': VK_COMMA, '.': VK_PERIOD,
			' ': VK_SPACE, '\n': VK_ENTER, '\t': VK_TAB
	]

	static robotCode( String c ) {
		def ch = c as char
		if ( ch.isLetterOrDigit() ) {
			return result( KeyEvent."VK_${ch.toUpperCase()}", ch.isUpperCase() )
		} else {
			def fromMap = keyMap[ c ]
			if ( fromMap ) result( fromMap, false )
			else result VK_SPACE, false
		}
	}

	private static result( int c, boolean shift ) {
		[ code: c, shift: shift ]
	}
}
