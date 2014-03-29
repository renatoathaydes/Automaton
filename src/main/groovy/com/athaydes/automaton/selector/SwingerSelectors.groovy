package com.athaydes.automaton.selector

import com.athaydes.automaton.SwingUtil

import java.awt.*

/**
 * @author Renato
 */
class SwingerSelectors {

	/**
	 * Creates a selector which will perform a breadth-first search for
	 * all Components with a given name.
	 * @return byName selector
	 */
	static SimpleSwingerSelector byName( ) {
		new SimpleSwingerSelector() {
			@Override
			boolean matches( String selector, Component component ) {
				component.name == selector
			}

			@Override
			String toString( ) { "NameSwingerSelector" }

		} as SimpleSwingerSelector
	}

	/**
	 * Creates a selector which will perform a breadth-first search for
	 * all Components containing some given text.
	 * <p/>
	 * This selector is extremely flexible and will find text in almost
	 * any Swing Component and even in non-Components such as:
	 * <ul>
	 *     <li>JTable's headers and cells</li>
	 *     <li>JCombo items</li>
	 *     <li>JTree TreeNodes</li>
	 * </ul>
	 * @return byText selector
	 */
	static SimpleSwingerSelector byText( ) {
		new SimpleSwingerSelector() {
			@Override
			boolean matches( String selector, Component component ) {
				SwingUtil.callMethodIfExists( component, "getText" ) == selector
			}

			@Override
			String toString( ) { "TextSwingerSelector" }

		} as SimpleSwingerSelector
	}

	/**
	 * Creates a selector which will perform a breadth-first search for
	 * all Components with a given type.
	 * <p/>
	 * The given type might be the fully-qualified name or the simple name.
	 * @return byName selector
	 */
	static SimpleSwingerSelector byType( ) {
		new SimpleSwingerSelector() {
			@Override
			boolean matches( String selector, Component component ) {
				SelectorHelper.instance.doesClassMatch( component.class, selector )
			}

			@Override
			String toString( ) { "TypeSwingerSelector" }

		} as SimpleSwingerSelector
	}

}
