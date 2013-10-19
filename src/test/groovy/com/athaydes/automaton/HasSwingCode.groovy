package com.athaydes.automaton

import javax.swing.*

/**
 * Tests that have Swing code can implement this interface to benefit from
 * mixing in helper Swing Categories
 * User: Renato
 */
public interface HasSwingCode extends HasMixins {

	JFrame getJFrame( )

}