package com.athaydes.automaton.internal

import com.athaydes.automaton.SwingAutomaton
import groovy.swing.SwingBuilder
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import javax.swing.*
import java.awt.*

/**
 *
 * User: Renato
 */
class RobotTypingUtilTest {

	static JFrame jFrame
	static JTextArea jta

	@BeforeClass
	static void setup() {
		new SwingBuilder().edt {
			jFrame = frame( title: 'Frame', size: [ 300, 300 ] as Dimension, show: true ) {
				jta = textArea()
			}
		}

		sleep 500
		assert jta != null
		assert jta.text == ''
	}

	@Before
	void start() {
		jta.text = ''
	}

	@AfterClass
	static void cleanup() {
		jFrame?.dispose()
		jta = null
		jFrame = null
	}

	@Test
	void testDigits() {
		def text = '1234567890'
		SwingAutomaton.user.moveTo( jta ).click()
				.type( text ).pause( 100 )
		assert jta.text == text
	}

	@Test
	void testSpecialChars() {
		def text = ' ,./+-*/\\;:"\'<>?[]{}|!@#$%^&*()_='
		SwingAutomaton.user.moveTo( jta ).click()
				.type( text ).pause( 100 )
		assert jta.text == text
	}

	@Test
	void testLowerCaseLetters() {
		def text = 'abcdefghijklmnopqrstuvxzwy'
		SwingAutomaton.user.moveTo( jta ).click().type( text ).pause( 100 )
		assert jta.text == text
	}

	@Test
	void testUpperCaseLetters() {
		def text = 'ABCDEFGHIJKLMNOPQRSTUVXZWY'
		SwingAutomaton.user.moveTo( jta ).click().type( text ).pause( 100 )
		assert jta.text == text
	}

}
