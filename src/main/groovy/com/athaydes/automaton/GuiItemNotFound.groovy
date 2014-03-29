package com.athaydes.automaton

/**
 * @author Renato
 */
class GuiItemNotFound extends RuntimeException {

	GuiItemNotFound( String message ) {
		super( message )
	}

	GuiItemNotFound( String message, Throwable cause ) {
		super( message, cause )
	}

}
