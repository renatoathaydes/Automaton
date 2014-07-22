package com.athaydes.automaton.selector
/**
 * @author Renato
 */
interface AutomatonSelector<K> {

	/**
	 * Gets all items satisfying this selector using the given parameters.
	 * @param prefix used to route to this selector
	 * @param selector query
	 * @param root of the hierarchical GUI structure
	 * @param limit maximum number of items to return
	 * @return all items satisfying this selector
	 */
	List<K> apply( String prefix, String selector, K root, int limit )


    /**
     * Gets all items satisfying this selector using the given parameters.
     * @param prefix used to route to this selector
     * @param selector query
     * @param root of the hierarchical GUI structure
     * @return all items satisfying this selector
     */
    List<K> apply( String prefix, String selector, K root )

}
