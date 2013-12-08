package com.athaydes.automaton.selector

import com.athaydes.automaton.SwingUtil
import javafx.scene.Node

/**
 * @author Renato
 */
class FxSelectors {

	static AutomatonSelector<Node> byText() { TextFxSelector.instance }

}

@Singleton
class TextFxSelector extends SimpleFxSelector {

	@Override
	boolean matches( String selector, Node node ) {
		def text = SwingUtil.callMethodIfExists( node, 'getText' )
		text != null && text == selector
	}

	@Override
	boolean followPopups() { true }

}
