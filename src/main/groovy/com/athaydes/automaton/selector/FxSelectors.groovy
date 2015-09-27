package com.athaydes.automaton.selector

import com.athaydes.automaton.SwingUtil
import javafx.scene.Node

/**
 * @author Renato
 */
class FxSelectors {

	static AutomatonSelector<Node> byText() { TextFxSelector.instance }

	static AutomatonSelector<Node> byId() { IdFxSelector.instance }

	static AutomatonSelector<Node> byStyleClass() { StyleClassFxSelector.instance }

	static AutomatonSelector<Node> byType() { TypeFxSelector.instance }

}

@Singleton
class TextFxSelector extends SimpleFxSelector {

	@Override
	boolean matches( String selector, Node node ) {
		try {
  			def text = SwingUtil.callMethodIfExists( node, 'getText' )
  			return text != null && text == selector
		} catch ( MissingMethodException ignored ) {
  			return false
    }
	}

	@Override
	boolean followPopups() { true }

}

@Singleton
class IdFxSelector extends SimpleFxSelector {

	@Override
	boolean matches( String selector, Node node ) {
		node.id ? node.id == selector : false
	}

	@Override
	boolean followPopups() { true }

}

@Singleton
class StyleClassFxSelector extends SimpleFxSelector {

	@Override
	boolean matches( String selector, Node node ) {
		selector in node.styleClass
	}

	@Override
	boolean followPopups() { true }

}


@Singleton
class TypeFxSelector extends SimpleFxSelector {

	@Override
	boolean matches( String selector, Node node ) {
		SelectorHelper.instance.doesClassMatch( node.class, selector )
	}

	@Override
	boolean followPopups() { true }

}
