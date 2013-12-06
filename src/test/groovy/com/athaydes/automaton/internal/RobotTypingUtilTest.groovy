package com.athaydes.automaton.internal

import com.athaydes.automaton.SwingAutomaton
import groovy.swing.SwingBuilder
import org.junit.After
import org.junit.Test

import javax.swing.*
import java.awt.*

/**
 *
 * User: Renato
 */
class RobotTypingUtilTest {

	JFrame jFrame

	@After
	void cleanup() {
		jFrame?.dispose()
	}

	@Test
	void testRobotCode() {
		JTextArea jta
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: true ) {
				jta = textArea()
			}
		}

		sleep 500
		assert jta != null
		assert jta.text == ''

		def text = 'ABCDEFGHIJKLMNOPQRSTUVXZWY'
		SwingAutomaton.user.moveTo( jta ).click().type( text ).pause( 100 )
		assert jta.text == text
		jta.text = ''

		text = 'abcdefghijklmnopqrstuvxzwy'
		SwingAutomaton.user.moveTo( jta ).click().type( text ).pause( 100 )
		assert jta.text == text
		jta.text = ''

		text = '1234567890 ,./+*/'
		SwingAutomaton.user.moveTo( jta ).click()
				.type( text ).pause( 100 )
		assert jta.text == text
		jta.text = ''
	}

}
