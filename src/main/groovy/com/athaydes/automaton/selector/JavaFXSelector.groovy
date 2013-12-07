package com.athaydes.automaton.selector

import javafx.scene.Node

/**
 * @author Renato
 */
@Singleton
class JavaFXSelector implements AutomatonSelector<Node> {

	@Override
	List<Node> apply( String prefix, String selector, Node root, int limit = Integer.MAX_VALUE ) {
		root.lookupAll( prefix == 'type:' ? selector : prefix + selector ).take( limit ).toList() as List<Node>
	}

}
