# Swing - Advanced Usage


### Swinger Selectors

The `Swinger` uses a powerful mechanism to allow you to find just about anything you want.
This mechanism is easily extensible, which means that if the `Automaton` does not provide what you need, you can write it yourself and use it seamlessly with the rest of the framework.

Let's see how that works.

* Selecting by name
  * `"name:component-name"` or simply `"component-name"` (if no custom selectors added)
* Selecting by type
  * `"type:JButton` or `"type:javax.swing.JButton`
* Selecting by text (works with any Component which has text, and also with `JTree` nodes and `JTable` headers/cells):
  * `"text:Component label"`
* Writing a selector to extend Automaton:
```java
Swinger swinger = Swinger.forSwingWindow();
swinger.setSpecialPrefixes( createCustomSelectors() );
-----------------------------------------------------------------
private Map<String, Closure<Component>> createCustomSelectors() {

    // we must use a sorted map as the first entry in the map is used if no prefix is given
    // eg. in this case, click( "cust:field" ) would be the same as click( "field" )
    Map<String, Closure<Component>> customSelectors = new LinkedHashMap<String, Closure<Component>>();
    customSelectors.put( "cust:", new SwingerSelector( this ) {
        @Override
        public Component call( String selector, Component component ) {
            // this custom selector does almost exactly the same thing as Automaton's default selector
            // except that it turns all characters lower-case before looking up by name
            return SwingUtil.lookup( selector.toLowerCase(), component );
        }
    } );

    // it is always good to keep Automaton's default selectors active
    customSelectors.putAll( Swinger.getDEFAULT_PREFIX_MAP() );
    return customSelectors;
}
---------------------------------------------------------------------
// Now we can use the custom selector as follows
swinger.clickOn( "cust:Text-AreA" );
Component c = swinger.getAt( "cust:Another" );
```
Notice that the first item returned by the Map you use to specify custom selectors is used by default if no prefix is given. This is the reason why selecting `"comp"` is the same as `"name:comp"` in `Automaton` unless you modify that! In the example above, as the first item in the Map is the `"cust:"` selector, the `"cust:"` selector will be used by default.

Notice that there is nothing special about the `:` in the prefixes, it's just the convention adopted in `Automaton`. You could just as well have used prefixes like `"myprefix>>>"`, which would then be used like this: `clickOn( "myprefix>>>Something" ) `.

#### SwingUtil

The `SwingUtil` class contains a few helpful methods which you can also use directly to locate components, or inside your custom selectors, as in the example above, which uses `lookup( String name, Component root )` (finds Components by name).

Some other methods:

* `lookupAll( String name, Component root, int limit )` - find all Components with the given name. The `limit` argument is optional and if given, limits how many components will be returned.
* `text( String textToFind, Component root )` - used by `Automaton`'s own `"text:"` selector, finds Components, JTree nodes and JTable cells/headers by their text.
* `textAll( String textToFind, Component root, int limit )` - as `text(..)` but returns all items matching the selector.
* `type( String type, Component root )` - finds a Component by its Java type. The type must be exactly the same (not just instanceof).
* `typeAll( String type, Component root, int limit )` - as `type(..)` but returns all items matching the selector.
* `navigateBreadthFirst( Component | JTree | JTable root, Closure action )` - navigates through the Swing Component tree or JTree hierarchy, passing each visited component to the given Closure (the `JTable` version also passes `row` and `column` indexes to the Closure). To stop navigating, the Closure must return `true`.
* `callMethodIfExists( Object obj, String methodName, Object... args )` - very useful if you want to safely try to call a certain method on instances of different classes when possible (returns an empty List if the method does not exist). Very useful inside the action Closure when navigating through a tree.


### Making efficient lookups

> Notice that although it is quite convenient to use the JFrame as the "root" component for all searches,
> it is often better to use the lookup method directly to find the top-level Component you're interested in first,
> and then use it as the "root" component (see example below).
> That's because always using the same "root" component (the `frame` in the example above) as the starting point of
> the search algorithm can be slow or, worse, return unrelated Components which happened to have the same name as the
> one you're actually looking for!

If you are simply testing a single component inside your application, you should do something like shown below
(to restrict the search space to inside the `compUnderTest` hierarchy):

```java
JFrame frame = app.getjFrame();

Component compUnderTest = SwingUtil.lookup( "component-under-test", frame );
Swinger.getUserWith( compUnderTest )
       .moveTo( "draggable1" ).dragBy( 50, 0 );
```
