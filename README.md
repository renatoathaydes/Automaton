# Automaton
* easy tests for Swing and JavaFX applications
* by Renato Athaydes

Automaton is a framework which makes it easy to test Java GUIs developed with Swing, JavaFX 2, or both.

It is written in Groovy but the examples shown in this page are in Java (because most users are expected
to be using Java).

## Requirements

To use *Automaton*, you need to clone it from Git and build it with [Gradle](http://www.gradle.org) (or your compilation tool of choice)
in your local machine.
When you build, make sure to either skip the tests or prepare to leave your machine under Automaton's control for a couple of minutes
as it runs all the tests (100% test coverage is a goal in this project, so there's a lot of tests).

If I get a request, I can just email you the jar or even try to get it on the Maven Central.

Please let me know if you would have interest! My email is renato@athaydes.com

To see this project dependencies, check the build.gradle file in the root folder.

## Providing custom Configuration

The `Automaton` does not require any external configuration, but it allows the user to provide a configuration file which will be used if found.

To provide custom configuration, simply add a file called `/automaton-config.properties` to the classpath.

The config file contains the following properties:

```properties
# Set the DEFAULT speed to be used by the Automaton when no speed is passed in a method call
# Options are the values of enum: com.athaydes.automaton.Speed
automaton.speed=VERY_FAST
```

## How to use it

*For `Swinger` quick-reference, go to the [Swinger cheat-sheet](swing-cheat-sheet.md)*

### Platform-independent tests

You can test or automate anything that a user can interact with in a computer using *Automaton*.
This is the simplest possible usage, but also the most limited:

```java
Automaton.getUser().moveTo( 0, 0 ).moveBy( 50, 100 ).click()
         .moveBy( 30, 0 ).rightClick().moveBy( 30, 50 )
         .click().dragBy( 50, 100, Speed.SLOW )
         .pause( 1000 ).type( "Automaton" ).type( KeyEvent.VK_ENTER );
```

All other drivers sub-class the Automaton, so they will all inherit its methods.

### Swing applications

Swing applications can be driven by 2 different drivers. Which one to use depends purely on your taste.
The `SwingAutomaton` can be more efficient if used correctly (because it encourages you to select Components more
carefully), but the `Swinger` is much easier to use.

#### The SwingAutomaton

By using the SwingAutomaton 'driver', you can interact with applications written with Java Swing much more easily.
Here's an example of how to use it:

```java
JFrame frame = app.getjFrame();
Component clickMe = SwingUtil.lookup( "button1-name", frame );
Component draggable1 = SwingUtil.lookup( "draggable1", frame );

SwingAutomaton.getUser().clickOn( clickMe )
              .moveTo( draggable1 ).dragBy( 50, 0 );
```

The Strings passed to the lookup method ( SwingUtil is a utility class provided by *Automaton* ) are the *name* of
the Swing components... so in order to be able to select a Swing Component with this method, its name must be specified.

The next section shows how the `Swinger` allows much more flexible selection of Components by other means, among other
convenient features for Swing testers.


#### The Swinger

The other Swing 'driver', the `Swinger`, allows you to skip the `SwingUtil.lookup` call
and select components much more easily. It contains a powerful mechanism which allow you to select exactly what you want in a flexible, extensible way. It will be explained in the next sections, but first, let's look at what the example above could look like with the `Swinger`:

```java
JFrame frame = app.getjFrame();

Swinger.getUserWith( frame ).clickOn( "button1-name" )
       .moveTo( "draggable1" ).dragBy( 50, 0 );
```
You can even use the Swinger to locate the JFrame for you automatically, so in case your app developers forgot to make it easy for you as a tester to get it, the `Swinger` will save you lots of time!

```java
Swinger swinger = Swinger.forSwingWindow();
```

See the `SwingerSample` for more examples:

blob/master/src/test/java/com/athaydes/automaton/samples/SwingerSample.java

#### Swinger Selectors

So far, we have seen only selection by name, ie. we are only able to select components by their name. This is not ideal because often times, the Component you want to select is exactly the one that the developers forgot to name! Not to mention that you might need to select certain items which do not even have a name, such as a `JTree` node.

The `Swinger` uses a powerful mechanism to allow you to find just about anything you want. This mechanism is easily extensible, which means that if the `Automaton` does not provide what you need, you can write it yourself and use it seamlessly with the rest of the framework.

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

#### Making efficient lookups

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

### JavaFX applications

Just like Swing has 2 'drivers', so does JavaFX. However, no equivalent to `SwingUtil` exists for JavaFX because every
`Node` in JavaFX has the ability to lookup other Nodes within it.

#### The FXAutomaton:

```java
Node root = app.getScene().getRoot();
FXAutomaton.getUser().clickOn( root.lookup( "#button-id" ) )
           .moveTo( root.lookup( ".css-class-selector" ) ).rightClick();
```

#### The FXer:

```java
Node root = app.getScene().getRoot();
FXer.getUserWith( root ).clickOn( "#button-id" )
    .moveTo( ".css-class-selector" ).rightClick();
```

Notice that the same warning about restricting your search space given in the Swing section is also valid in JavaFX.
Basically, try to use a good starting-point for your searches rather than always use the root.

### Mixed Swing/JavaFX applications

If you have a big Swing application but want to still enjoy the capabilities of JavaFX (beautiful widgets,
free effects, the web-view and many others), I hope you know [you can embed JavaFX into Swing quite easily!]
(http://docs.oracle.com/javafx/2/swing/swing-fx-interoperability.htm#CHDIEEJE).

You can check the [mixed Swing/JavaFX sample interface](https://github.com/renatoathaydes/Automaton/blob/master/src/test/groovy/com/athaydes/automaton/samples/SwingJavaFXSampleAppTest.groovy)
I've created myself for testing the *Automaton*, written in Groovy.

But you're probably interested in knowing how you'll be able to test such a mixed application!
Easily, with the `SwingerFXer` *Automaton* driver:

```java
JFrame frame = swingJavaFx.getjFrame();
JFXPanel fxPanel = swingJavaFx.getJfxPanel();
String swingTextAreaText = "Hello, I am Swing...";
String fxInputText = "Hello, JavaFX...";

SwingerFxer.userWith( frame, fxPanel.getScene().getRoot() )
    .doubleClickOn( "text:colors" )
    .clickOn( "text-area" )
    .type( swingTextAreaText ).pause( 1000 )
    .clickOn( "#left-color-picker" ).pause( 1000 )
    .moveBy( 60, 40 ).click().pause( 1000 )
    .clickOn( "#fx-input" )
    .type( fxInputText )
    .moveBy( 100, 0 ).pause( 500 );

JTextArea jTextArea = ( JTextArea ) lookup( "text-area", frame );
TextField textField = ( TextField ) fxPanel.getScene().lookup( "#fx-input" );
ColorPicker leftPicker = ( ColorPicker ) fxPanel.getScene().lookup( "#left-color-picker" );

assertEquals( swingTextAreaText, jTextArea.getText() );
assertEquals( fxInputText, textField.getText() );
assertEquals( leftPicker.getValue(), swingJavaFx.getTextLeftColor() );
```

The above is a copy of part of the SwingJavaFXSampleAppTestInJava class in this project.
Just type `t` in the GibHub main page and search for this class for the complete code.

Notice the the `SwingerFxer` is a composite of `Swinger` and `FXer` and therefore contains all their methods.
String selectors work like this:

* if the selector starts with a `.` or `#`, the lookup is made in the JavaFX app.
* in all other cases, the lookup re-directed to the Swinger (which can use built-in and custom selectors).

Pretty simple!

If you want to lookup a JavaFX Node, you'll use a css selector (starting with `.` = by css class, `#` by ID).
To lookup a Swing Component, just use the `Swinger` selector syntax (see `Swinger` section).


## Other useful things

#### Testing pure JavaFX Apps

If all you want is to test a pure JavaFX Application, you may want to check out [TestFX](https://github.com/SmartBear/TestFX).
The driver looks basically the same as the *FXer*, but it has more capabilities and other features such as very useful
Hamcrest matchers for powerful assertions. In fact, both *TestFX* and *Automaton* were born out of the same SmartBear
team which develops [LoadUI](http://loadui.org), which is a powerful API load testing tool written on JavaFX.


#### Avoid timing issues in GUI testing

Timing issues are a big problem with any GUI testing.
*Automaton* is concerned with making it easy to drive applications, but you will find that your code may end up having
lots of `pause(250)` calls to allow for small delays in the GUI response to commands.

A very good way to make tests more robust is to use the [tempus-fugit](http://tempusfugitlibrary.org/) library which
provides excellent concurrent, asynchronous and waiting mechanisms that will help you bring determinism back into your
GUI testing!

I found the `waitOrTimeout` function particularly useful! You can write awesome code like this one (in Groovy):

```groovy
fxer.clickOn( "#open-panel" )
waitOrTimeout( [ isSatisfied: { root.lookup("#new-panel") != null } ] as Condition,
                  timeout( seconds( 5 ) ) )
fxer.clickOn( "#new-panel" ).type( "Automaton and tempus-fugit are awesome" )
```

Check it out and you won't regret!




> Written with [StackEdit](https://stackedit.io/).