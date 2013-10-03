package com.athaydes.automaton.internal

import com.athaydes.automaton.SwingAutomaton
import groovy.swing.SwingBuilder
import org.junit.After
import org.junit.Test

import javax.swing.*
import java.awt.Dimension

import static java.awt.event.KeyEvent.VK_ALT
import static java.awt.event.KeyEvent.VK_SHIFT

/**
 *
 * User: Renato
 */
class RobotTypingUtilTest {

	JFrame jFrame

	@After
	void cleanup( ) {
		jFrame?.dispose()
	}

	@Test
	void testRobotCode( ) {
		JTextArea jta
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: true ) {
				jta = textArea()
			}
		}

		sleep 500
		assert jta != null
		assert jta.text == ''


		def testCommonChars = {
			SwingAutomaton.user.moveTo( jta ).click().type( 'ABCDEFGHIJKLMNOPQRSTUVXZWY' ).pause( 100 )
			assert jta.text == 'ABCDEFGHIJKLMNOPQRSTUVXZWY'
			jta.text = ''

			SwingAutomaton.user.moveTo( jta ).click().type( 'abcdefghijklmnopqrstuvxzwy' ).pause( 100 )
			assert jta.text == 'abcdefghijklmnopqrstuvxzwy'
			jta.text = ''

			SwingAutomaton.user.moveTo( jta ).click()
					.type( '1234567890 ,./-\n/+*\t' ).pause( 100 )
			assert jta.text == '1234567890 ,./-\n/+*\t'
			jta.text = ''
		}

		// try this in 3 different languages
		3.times {
			testCommonChars()

			// FIXME Find way to change input language in any machine (haha)
			// THIS CHANGES THE INPUT LANGUAGE ONLY IN MY MACHINE!!!
			SwingAutomaton.user.pressSimultaneously( VK_ALT, VK_SHIFT ).pause( 500 )
		}
	}

}
