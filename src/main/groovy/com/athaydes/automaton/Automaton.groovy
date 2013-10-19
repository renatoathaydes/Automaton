package com.athaydes.automaton

import com.athaydes.internal.Config
import com.athaydes.internal.Mouse
import com.athaydes.internal.TimeLimiter

import java.awt.*
import java.awt.event.KeyEvent
import java.util.concurrent.TimeUnit

import static com.athaydes.internal.RobotTypingUtil.robotCode

/**
 *
 * User: Renato
 */
class Automaton<T extends Automaton> {

	protected final robot = new Robot()
	static DEFAULT = Config.instance.speed
	private static Automaton instance
	private abortAfter = new TimeLimiter().&abortAfter

	static synchronized T getUser( ) {
		if ( !instance ) instance = new Automaton<Automaton>()
		instance as T
	}

	protected Automaton( ) {}

	T moveTo( Number x, Number y, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = new Point( x.intValue(), y.intValue() )
		move( currPos, target, speed )
	}

	T moveBy( Number x, Number y, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = new Point( ( currPos.x + x ).intValue(),
				( currPos.y + y ).intValue() )
		move( currPos, target, speed )
	}

	protected T move( currPos, target, Speed speed ) {
		abortAfter( {
			while ( currPos.x != target.x || currPos.y != target.y ) {
				robot.mouseMove delta( currPos.x, target.x ), delta( currPos.y, target.y )
				robot.delay speed.delay
				currPos = MouseInfo.pointerInfo.location
			}
			this as T
		}, 1, TimeUnit.MINUTES )
	}

	protected static int delta( curr, target ) {
		def comp = curr.compareTo target
		curr + ( comp > 0 ? -1 : comp == 0 ? 0 : 1 ) as int
	}

	T dragBy( Number x, Number y, Speed speed = DEFAULT ) {
		robot.mousePress Mouse.LEFT
		moveBy x, y, speed
		robot.mouseRelease Mouse.LEFT
		this as T
	}

	DragOn<T> dragFrom( Number x, Number y ) {
		new DragOn( this, x, y )
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

	T doubleClick( ) {
		click().click()
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

class DragOn<T extends Automaton> {
	final fromX
	final fromY
	final T automaton

	protected DragOn( T automaton, fromX, fromY ) {
		this.automaton = automaton
		this.fromX = fromX
		this.fromY = fromY
	}

	T onto( Number x, Number y, Speed speed = Automaton.DEFAULT ) {
		automaton.moveTo( fromX as int, fromY as int, speed )
		automaton.dragBy( x - fromX as int, y - fromY as int, speed )
	}
}

enum Speed {
	SLOW( 10 ), MEDIUM( 7 ), FAST( 4 ), VERY_FAST( 1 )
	final int delay

	private Speed( int delay ) {
		this.delay = delay
	}
}
