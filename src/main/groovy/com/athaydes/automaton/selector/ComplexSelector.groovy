package com.athaydes.automaton.selector

import static com.athaydes.automaton.selector.MatchType.ALL
import static com.athaydes.automaton.selector.MatchType.ANY

/**
 * @author Renato
 */
class ComplexSelector {

	final String[] selectors
	MatchType matchType

	protected ComplexSelector( MatchType matchType, String... selectors ) {
		this.matchType = matchType
		this.selectors = selectors
	}



	@Override
	public java.lang.String toString( ) {
		"ComplexSelector(matchType:$matchType,selectors:$selectors)";
	}
}

class ComplexSelectorWithAntiSelectors extends ComplexSelector {

	ComplexSelector antiSelector

	protected ComplexSelectorWithAntiSelectors( MatchType matchType, String... selectors ) {
		super( matchType, selectors )
	}

	ComplexSelector except( String selector ) {
		antiSelector = new ComplexSelector( ANY, selector )
		this
	}

	ComplexSelector except( ComplexSelector selector ) {
		antiSelector = selector
		this
	}

}

enum MatchType {
	ANY, ALL
}


class StringSwingerSelectors {

	static ComplexSelectorWithAntiSelectors matchingAny( String query, String... queries ) {
		new ComplexSelectorWithAntiSelectors( ANY, ( [ query ] + queries.toList() ) as String[] )
	}

	static ComplexSelectorWithAntiSelectors matchingAll( String query, String... queries ) {
		new ComplexSelectorWithAntiSelectors( ALL, ( [ query ] + queries.toList() ) as String[] )
	}

}

