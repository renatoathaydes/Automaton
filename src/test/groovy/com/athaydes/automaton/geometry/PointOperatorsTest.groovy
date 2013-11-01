package com.athaydes.automaton.geometry

import spock.lang.Specification

import java.awt.*

/**
 * @author Renato
 */
class PointOperatorsTest extends Specification {

	def "Points can be added as in Euclidean geometry"( ) {
		expect:
		use( PointOperators ) {
			( p1 as Point ) + ( p2 as Point ) == result as Point
		}

		where:
		p1        | p2         | result
		[ 0, 0 ]  | [ 0, 0 ]   | [ 0, 0 ]
		[ 10, 0 ] | [ 0, 0 ]   | [ 10, 0 ]
		[ 0, 10 ] | [ 0, 0 ]   | [ 0, 10 ]
		[ 2, 5 ]  | [ 3, 6 ]   | [ 5, 11 ]
		[ 1, 2 ]  | [ -3, -5 ] | [ -2, -3 ]
	}


}
