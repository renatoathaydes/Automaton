package com.athaydes.internal

import java.awt.event.KeyEvent

import static java.awt.event.KeyEvent.*

/**
 *
 * User: Renato
 */
class RobotTypingUtil {

	static final keyMap = [
			'*' : result( VK_MULTIPLY, false ),
			'-' : result( VK_MINUS, false ),
			'+' : result( VK_ADD, false ),
			'/' : result( VK_DIVIDE, false ),
			',' : result( VK_COMMA, false ),
			'.' : result( VK_PERIOD, false ),
			' ' : result( VK_SPACE, false ),
			'\n': result( VK_ENTER, false ),
			'\t': result( VK_TAB, false ),
			':' : result( VK_COLON, true ),
			';' : result( VK_SEMICOLON, false ),
			'!' : result( VK_EXCLAMATION_MARK, true ),
			'@' : result( VK_AT, true ),
			'#' : result( VK_NUMBER_SIGN, true ),
			'$' : result( VK_DOLLAR, true ),
			'^' : result( VK_CIRCUMFLEX, true ),
			'&' : result( VK_AMPERSAND, true ),
			'(' : result( VK_LEFT_PARENTHESIS, false ),
			')' : result( VK_RIGHT_PARENTHESIS, false ),
			'[' : result( VK_OPEN_BRACKET, false ),
			']' : result( VK_CLOSE_BRACKET, false ),
			'{' : result( VK_BRACELEFT, true ),
			'}' : result( VK_BRACERIGHT, true ),
			'"' : result( VK_QUOTEDBL, true ),
			"'" : result( VK_QUOTE, false ),
			'\\': result( VK_BACK_SLASH, false ),
			'<' : result( VK_LESS, false ),
			'>' : result( VK_GREATER, true ),
			'?' : result( VK_SLASH, true ),
			'=' : result( VK_EQUALS, false ),
			'_' : result( VK_UNDERSCORE, true ),
			'|' : result( VK_BACK_SLASH, true ),
			'%' : result( VK_5, true )
	]

	static robotCode( String c ) {
		def ch = c as char
		if ( ch.isLetterOrDigit() ) {
			return result( KeyEvent."VK_${ch.toUpperCase()}", ch.isUpperCase() )
		} else {
			def fromMap = keyMap[ c ]
			if ( fromMap ) return fromMap
			else result VK_SPACE, false
		}
	}

	private static result( int c, boolean shift ) {
		[ code: c, shift: shift ]
	}
}
