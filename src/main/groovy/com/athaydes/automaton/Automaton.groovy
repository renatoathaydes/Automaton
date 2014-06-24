package com.athaydes.automaton

import com.athaydes.automaton.cli.AutomatonDemo
import com.athaydes.automaton.cli.AutomatonScriptRunner
import com.athaydes.internal.Config
import com.athaydes.internal.Interaction
import com.athaydes.internal.Mouse
import com.athaydes.internal.TimeLimiter

import java.awt.*
import java.awt.event.KeyEvent
import java.util.concurrent.TimeUnit

import static com.athaydes.internal.RobotTypingUtil.robotCode
import static com.sun.glass.ui.Application.GetApplication

/**
 *
 * User: Renato
 */
class Automaton<T extends Automaton> {

    def robot = isMac() ? GetApplication().createRobot() : new Robot()

    static Speed DEFAULT = Config.instance.speed
    private static Automaton instance
    private abortAfter = new TimeLimiter().&abortAfter
    final Interaction interaction
	/**
	 * Get the singleton instance of Automaton, which is lazily created.
	 * @return Automaton singleton instance
	 */
	static synchronized Automaton<Automaton> getUser() {
		if ( !instance ) instance = new Automaton<Automaton>()
		instance
	}

    private static isMac() {
        System.getProperty( "os.name" )?.toLowerCase()?.contains( "mac" )
    }

	protected Automaton() {
		this.interaction = Interaction.instance
	}

	T moveTo( Number x, Number y, Speed speed = DEFAULT ) {
		moveTo( new Point( x.intValue(), y.intValue() ), speed )
	}

	T moveTo( Point target, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		move( currPos, target, speed )
	}

	T moveTo( Closure<Point> getTarget, Speed speed = DEFAULT ) {
		def target = null
		while ( !target || getTarget() != target ) {
			target = getTarget()
			moveTo( target, speed )
		}
		this as T
	}

	T moveBy( Number x, Number y, Speed speed = DEFAULT ) {
		def currPos = MouseInfo.pointerInfo.location
		def target = new Point( ( currPos.x + x ).intValue(),
				( currPos.y + y ).intValue() )
		move( currPos, target, speed )
	}

	protected T move( currPos, target, Speed speed ) {
		interaction.await "Move mouse to $target"
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

	T click() {
		interaction.await "Click"
		doClick Mouse.LEFT
	}

	T rightClick() {
		interaction.await "Right-click"
		doClick Mouse.RIGHT
	}

	T doubleClick() {
		interaction.await "Double-click"
		doClick( Mouse.LEFT ).pause( 50 ).doClick( Mouse.LEFT )
	}

	protected T doClick( button ) {
		robot.mousePress button
		robot.mouseRelease button
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
		def keysText = keyCodes.collect { KeyEvent.getKeyText( it ) }
		interaction.await "Press simultaneously $keysText"
		try {
			keyCodes.each { robot.keyPress it }
		} finally {
			robot.delay 50
			try {
				keyCodes.each { robot.keyRelease it }
			} catch ( ignored ) {
			}
		}
		this as T
	}

	T type( String text, Speed speed = DEFAULT ) {
		interaction.await "Type $text"
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

	private static final void usage() {
		println '''Automaton usage:
		| - You must provide one of the following options to run Automaton:
		|    -demo - Shows a demo of Automaton with a built-in UI and a simple Automaton script
		|    -script <file> - runs your Automaton script'''.stripMargin()
	}

	static void main( String[] args ) {
		if ( !args ) {
			args = [ '-demo' ]
		}
		switch ( args[ 0 ] ) {
			case '-demo': demo( args.size() > 1 ? args[ 1 ] : null )
				break
			case '-script': if ( args.size() > 1 ) runScript( args[ 1 ] ) else usage()
				break
			default: usage()
		}
	}

	static void runScript( String fileName ) {
		AutomatonScriptRunner.instance.runScript( fileName )
	}

	static void demo( String option ) {
		AutomatonDemo.instance.runDemo( option )
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
