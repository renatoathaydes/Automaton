package com.athaydes.automaton.selector

import static com.athaydes.automaton.selector.MatchType.ALL
import static com.athaydes.automaton.selector.MatchType.ANY

/**
 * @author Renato
 */
class ComplexSelector {

	final String[] queries
	MatchType matchType

	protected ComplexSelector( MatchType matchType, String... queries ) {
		this.matchType = matchType
		this.queries = queries
	}

	@Override
	public String toString() {
		"ComplexSelector(matchType:$matchType,queries:$queries)";
	}
}

enum MatchType {
	ANY, ALL
}


class StringSelectors {

	static ComplexSelector matchingAny( String query, String... queries ) {
		new ComplexSelector( ANY, ( [ query ] + queries.toList() ) as String[] )
	}

	static ComplexSelector matchingAll( String query, String... queries ) {
		new ComplexSelector( ALL, ( [ query ] + queries.toList() ) as String[] )
	}

}

