# Automaton
* easy tests for Swing and JavaFX applications
* by Renato Athaydes

Automaton is a framework which makes it easy to test Java GUIs developed with Swing, JavaFX 2, or both.

It is written in Groovy but the examples shown in this page are in Java (because most users are expected
to be using Java).

## Requirements

To use *Automaton*, you need to clone it from Git and build it with Maven (or your compilation tool of choice)
in your local machine.

If I get a request, I can just email you the jar or even try to get it on the Maven Central.

Please let me know if you would have interest! My email is renato@athaydes.com

To see this project dependencies, check the pom.xml file in the root folder.

## How to use it

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
carefully), but the `Swinger` is easier to use.

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
 the Swing components... so in order to be able to select a Swing Component with *Automaton*, its name must be specified.

#### The Swinger

For convenience, there is another Swing 'driver', the `Swinger`, which allows you to skip the `SwingUtil.lookup` call
and select components more easily. The same example above could be written like this:

```java
JFrame frame = app.getjFrame();

Swinger.getUserWith( frame ).clickOn( "button1-name" )
       .moveTo( "draggable1" ).dragBy( 50, 0 );
```

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
           .clickOn( "text-area" )
           .type( swingTextAreaText ).pause( 1000 )
           .clickOn( "#left-color-picker" )
           .pause( 1000 ).moveBy( 60, 40 ).click()
           .pause( 1000 ).clickOn( "#fx-input" )
           .type( fxInputText ).moveBy( 100, 0 )
           .pause( 500 );

JTextArea jTextArea = ( JTextArea ) lookup( "text-area", frame );
TextField textField = ( TextField ) fxPanel.getScene().lookup( "#fx-input" );

assertEquals( swingTextAreaText, jTextArea.getText() );
assertEquals( fxInputText, textField.getText() );
assertEquals( "0x999999ff", swingJavaFx.getTextLeftColor().toString() );
```

The above is a copy of part of the SwingJavaFXSampleAppTestInJava class in this project.
Just type `t` in the GibHub main page and search for this class for the complete code.

Notice the the `SwingerFxer` is a composite of `Swinger` and `FXer` and therefore contains all their methods.
String selectors work like this:

* if the selector starts with a `.` or `#`, the lookup is made in the JavaFX app.
* in all other cases, the lookup is made in the Swing part of the app.

Pretty simple!

If you want to lookup a JavaFX Node, you'll use a css selector (starting with `.` = by css class, `#` by ID).
To lookup a Swing Component by name, just use the name (eg. looking up `something` will return any Component whose name
is exactly `something`).


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

