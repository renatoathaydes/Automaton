# Swing - Advanced Usage


### Swinger Selectors

The `Swinger` uses a powerful mechanism to allow you to find just about anything you want.
This mechanism is easily extensible, which means that if the `Automaton` does not provide what you need, you can write it yourself and use it seamlessly with the rest of the framework.

Let's see how that works.

### Existing selectors

#### By name

Selector:
```java
com.athaydes.automaton.selector.SwingerSelectors.byName()
```
Usage with `Swinger`:
```java
swinger.clickOn( "name:component-name" );
```
Because this is the default selector when using the `Swinger`, this is equivalent to:
```java
swinger.clickOn( "component-name" );
```

#### By type

Selector:
```java
com.athaydes.automaton.selector.SwingerSelectors.byType()
```
Usage with `Swinger`:
```java
swinger.clickOn( "type:javax.swing.JButton" );
```
Simple names can also be used:
```java
swinger.clickOn( "type:JButton" );
```
For type-safe usage, you can pass the class itself as a parameter to the `getAt` method:
```java
JButton button = swinger.getAt( JButton.class );
swinger.clickOn( button );
```
Notice that in **Groovy**, we can always replace a method call to `getAt` with a nicer syntax:
```groovy
JButton button = swinger[ JButton ]
```

#### By text

Selector:
```java
com.athaydes.automaton.selector.SwingerSelectors.byText()
```
Usage with `Swinger`:
```java
swinger.clickOn( "text:Text on my component" );
```

The text selector works with almost anything:

* any `Component` which has a `getText` method
* any `JTree`'s `TreeNode` using the `toString` method
* any `JTable` header or cell


### Example combining several selectors

```java
swinger.clickOn( "button-name" ).pause( 250 )
       .drag( "text:Drag me to the inbox" )
       .onto( "type:" + InboxWidget.class.getName() );
```

### Using complex selectors

The class `StringSwingerSelectors` contains the following factory methods to create complex
selectors:

  * `matchingAny` - any in the sequence of String selectors should be matched.
  * `matchingAll` - all in the sequence of String selectors must be matched.

You can use complex selectors as follows:

```java
swinger.clickOn( matchingAll( "type:TextField", "txt-field-1" ) )
       .moveTo( matchingAny( "text:Dont care about case", "text:Dont Care About Case" ) );
```


### Writing a selector to extend Automaton:

Create a custom selector:

```java
private Map<String, AutomatonSelector<Component>> createCustomSelectors() {
    Map<String, AutomatonSelector<Component>> customSelectors = new LinkedHashMap<>();

    // always keep the `Automaton` built-in selectors!
    customSelectors.putAll( Swinger.getDEFAULT_SELECTORS() );

    customSelectors.put( "cust:", new SimpleSwingerSelector() {
        @Override
        public boolean matches( String selector, Component component ) {
            // return true if the given component matches the selector
            return false;
        }
    } );

    return customSelectors;
}
```

Add the custom selector to a `Swinger` instance:

```java
Swinger swinger = Swinger.forSwingWindow();
swinger.setSelectors( createCustomSelector() );
```

Use the custom selector:
```java
swinger.clickOn( "cust:Text-AreA" );
Component c = swinger.getAt( "cust:Another" );
```

#### Note: you can change which selector is used by default

Notice that the first item of the selectors Map is used by default if no prefix is given (this is why we used a `LinkedHashMap`, which follows insertion order when iterating over items, in the example above).

This is the reason why selecting `"comp"` is the same as `"name:comp"` in `Automaton` unless you modify that!

#### What's up with the `:` in selectors?

There is nothing special about the `:` in the prefixes, it's just the convention adopted in `Automaton`. You could just as well have used prefixes like `"myprefix>>"`, which would then be used like this: `clickOn( "myprefix>>Something" ) `.

### Using SwingerSelectors directly

You don't need to use the `Swinger` to find components, you can use the selectors directly if you want to (although using the `Swinger` is almost always more convenient).

The `Swinger` itself uses these selector internally to find components.

```java
import static com.athaydes.automaton.selector.SwingerSelectors.byName;
import static com.athaydes.automaton.selector.SwingerSelectors.byType;
import static com.athaydes.automaton.selector.SwingerSelectors.byText;

// convenient way to get the root component
Component root = Swinger.forSwingWindow().getComponent();

// find the first 2 components named 'abc'
List<Component> components = byName().apply( "abc", root, 2 );

// find all `JButton`s
List<Component> components = byType().apply( "JButton", root );

// find anything with text 'Click me'
List<Component> components = byText().apply( "Click me", root );
```

### SwingUtil  - help when trying to find difficult items

The `SwingUtil` class contains a few helpful methods which you can also use directly to locate components.

Some examples:

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
