package com.athaydes.automaton.selector

/**
 * @author Renato
 */
@Singleton
class SelectorHelper {

	boolean doesClassMatch( Class cls, String selector ) {
		final isQualified = selector.contains( '.' )
		cls."${isQualified ? 'name' : 'simpleName'}" == selector
	}

}
