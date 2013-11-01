package com.athaydes.automaton.geometry

import java.awt.Point

/**
 * @author: renato
 */
@Category( Point )
class PointOperators {
	Point plus( Point other ) {
		new Point( ( this.x + other.x ).intValue(), ( this.y + other.y ).intValue() )
	}
}