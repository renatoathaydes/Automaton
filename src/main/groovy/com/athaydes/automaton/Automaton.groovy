package com.athaydes.automaton

import com.athaydes.internal.Mouse

import java.awt.*
import java.awt.event.KeyEvent

import static com.athaydes.internal.RobotTypingUtil.robotCode

/**
 *
 * User: Renato
 */
class Automaton<T extends Automaton> {

	protected final robot = new Robot()
	static final DEFAULT = Speed.FAST
	private static Automaton instance

	static synchronized T getUser( ) {
		if ( !instance ) instance = new Automaton<Automaton>()
		instance as T
	}

	protected Automaton( ) {}

	T moveTo( int x, int y, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = new Point( x, y )
		move( currPos, target, speed )
	}

	T moveBy( int x, int y, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = new Point( currPos.x + x as int, currPos.y + y as int )
		move( currPos, target, speed )
	}

	protected T move( currPos, target, Speed speed ) {
		while ( currPos.x != target.x || currPos.y != target.y ) {
			robot.mouseMove delta( currPos.x, target.x ), delta( currPos.y, target.y )
			robot.delay speed.delay
			currPos = MouseInfo.pointerInfo.location
		}
		this as T
	}

	protected static int delta( curr, target ) {
		def comp = curr.compareTo target
		curr + ( comp > 0 ? -1 : comp == 0 ? 0 : 1 ) as int
	}

	T dragBy( int x, int y, Speed speed = DEFAULT ) {
		robot.mousePress Mouse.LEFT
		moveBy x, y, speed
		robot.mouseRelease Mouse.LEFT
		this as T
	}

	T click( ) {
		robot.mousePress Mouse.LEFT
		robot.mouseRelease Mouse.LEFT
		this as T
	}

	T rightClick( ) {
		robot.mousePress Mouse.RIGHT
		robot.mouseRelease Mouse.RIGHT
		this as T
	}

	T pause( long millis ) {
		sleep millis
		this as T
	}

	T type( int keyCode ) {
		typeCode( false, keyCode )
		this as T
	}

	T pressSimultaneously( int ... keyCodes ) {
		try {
			keyCodes.each { robot.keyPress it }
		} finally {
			robot.delay 50
			try {
				keyCodes.each { robot.keyRelease it }
			} catch ( ignored ) {}
		}
		this as T
	}

	T type( String text, Speed speed = DEFAULT ) {
		text.each { c ->
			def rc = robotCode( c )
			typeCode rc.shift, rc.code, speed
		}
		this as T
	}

	void typeCode( boolean shift, int code, Speed speed = DEFAULT ) {
		if ( shift ) robot.keyPress KeyEvent.VK_SHIFT
		try {
			robot.keyPress code
			robot.delay speed.delay * 10
			robot.keyRelease code
		} finally {
			if ( shift ) robot.keyRelease KeyEvent.VK_SHIFT
		}
	}

}

enum Speed {
	SLOW( 10 ), MEDIUM( 7 ), FAST( 4 ), VERY_FAST( 1 )
	final int delay

	private Speed( int delay ) {
		this.delay = delay
	}
}
