package com.athaydes.automaton.selector

import com.athaydes.automaton.Swinger
import groovy.swing.SwingBuilder
import org.junit.Before
import org.junit.Test

import javax.swing.*
import java.awt.*
import java.util.concurrent.CountDownLatch

import static StringSelectors.matchingAll
import static StringSelectors.matchingAny

/**
 * @author Renato
 */
class ComplexSelectorTest {

	JFrame jFrame

	def vbox1
	def hbox1
	def label1
	def textArea1
	Swinger swinger

	@Before
	void before( ) {
		def latch = new CountDownLatch( 1 )
		new SwingBuilder().edt {
			jFrame = frame( name: 'frame', title: 'Frame', size: [ 300, 300 ] as Dimension,
					location: [ 150, 50 ] as Point, show: false ) {
				vbox1 = vbox( name: 'vbox' ) {
					hbox1 = hbox( name: 'hbox' ) {
						label1 = label( name: 'label', text: 'lbl' )
						textArea1 = textArea( name: 'text-area', text: 'txt-area' )
					}
				}
			}
			latch.countDown()
		}
		latch.await()
		swinger = Swinger.getUserWith( jFrame )
	}


	@Test
	void testMatchingAny( ) {
		assert ( swinger.getAll( matchingAny( 'type:Box') ) as Set ) == ( [ vbox1, hbox1 ] as Set )
		assert ( swinger.getAll( matchingAny( 'hbox', 'vbox') ) as Set ) == ( [ vbox1, hbox1 ] as Set )
	}

	@Test
	void testMatchingAll() {
		assert swinger.getAll( matchingAll( 'type:Box', 'vbox') ) == [ vbox1 ]
	}

}
