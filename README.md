# Automaton
* easy tests for Swing and JavaFX applications
* written for testers. Only basic coding skills required.


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

# Getting Started

## Swing Applications

Starting:
```java
// your code to start up an app
myApp.start();

// get a Swing-driver, or Swinger
Swinger swinger = Swinger.forSwingWindow();
```

Emulating user's actions in the GUI:
```java
swinger.clickOn( "text-input-1" )     // select by Component name (no prefix required)
       .type( "Hello Automaton!" )
       .drag( "text:Drag this item" ) // select by text (works with almost anything)
       .onto( "type:DropBoxImpl" );   // select by type

// get the tree nodes for the given tree path and open them
JTree myTree = swinger.getAt( JTree.class );
List<Component> nodesToOpen = SwingUtil.collectNodes( myTree,
                 "Project 1", "Test Suite A", "Test Case 1" );
swinger.doubleClickOn( nodesToOpen ); // open the Tree Nodes
```

Building complex selectors:

```java
import static com.athaydes.automaton.selector.StringSwingerSelectors.matchingAll;

swinger.clickOn( matchingAll( "type:MyDraggable", "text:Drag this item" ) );
```

Making assertions with `Automaton`'s Hamcrest matchers (using simple JUnit assertions):
```java
import static org.junit.Assert.assertThat;
import static com.athaydes.automaton.assertion.AutomatonMatcher.hasText;
import static com.athaydes.automaton.assertion.AutomatonMatcher.visible;

assertThat( swinger.getAt( "text-input-1" ), hasText( "Hello Automaton!" ) );

for ( Component node : nodesToOpen ) {
    assertThat( node, is( visible() ) );
}
```

Creating your own selectors:

```java
new SimpleSwingerSelector() {
    @Override
    public boolean matches( String selector, Component component ) {
        // return true if the given component matches the selector
        return false;
    }
};
```
See the [Swing Advanced Usage](swing-advanced.md) page for details.

### Comparison with FEST-Swing
Here's a code snippet copied from the FEST-Swing main page:
```java
dialog.comboBox("domain").select("Users");
dialog.textBox("username").enterText("alex.ruiz");
dialog.button("ok").click();
dialog.optionPane().requireErrorMessage()
                   .requireMessage("Please enter your password");
```
And here's how you would achieve the same thing with Automaton:
```java
swinger.clickOn( "domain" )
       .clickOn( "text:Users" )
       .clickOn( "username" )
       .type( "automaton" )
       .clickOn( "ok" ).pause( 250 );

assertThat( swinger.getAt( "message-area" ), hasText( "Please enter your password" ) );
```

With Automaton, when you write a test, you describe the actions a user would have taken within your application using a fluent API which reads as close to English as possible.

This is why it's so easy to write tests with Automaton!

With Fest, this is not the case. The code does not read anything like how a person would describe the actions the tester wants to have tested.

Sometimes, you just don't care about what type of control is used to implement the interface, so Automaton lets you decide whether or not you want to specify that. Omitting this information makes your tests more resilient to implementation changes (if perhaps less robust if you don't use unique names or are not careful restricting the search space during tests - see the [Swing Advanced Usage](swing-advanced.md) page for details).

### More Swing information

For `Swinger` quick-reference, go to the [Swinger cheat-sheet](swing-cheat-sheet.md).

The Automaton can be easily extended with the use of custom selectors. See the [Swing Advanced Usage](swing-advanced.md) page for details.


## JavaFX applications

Starting:

You can simply let the `Automaton` start your app:

```java
FXApp.startApp( new MyApp() );
FXer fxer = FXer.getUserWith( FXApp.getScene().getRoot() );
```

Or you can launch it using your own launcher, then create a FXer istance from any node at any time:

```java
FXer fxer = FXer.getUserWith( node );
```

Emulating user's actions in the GUI:
```java
fxer.clickOn( fxer.getAt( TextField.class ) )
    .type( "Automaton" )
    .clickOn( "#login-button" );
```

Making assertions with `Automaton`'s Hamcrest matchers:

```java
assertThat( fxer.getAt( "#message-area" ), hasText( "Please enter your password" ) );
```


## Mixed Swing/JavaFX applications

If you have a big Swing application but want to still enjoy the capabilities of JavaFX (beautiful widgets,
free effects, the web-view and many others), I hope you know [you can embed JavaFX into Swing quite easily!](http://docs.oracle.com/javafx/2/swing/swing-fx-interoperability.htm#CHDIEEJE).

You can check the [mixed Swing/JavaFX sample interface](https://github.com/renatoathaydes/Automaton/blob/master/src/test/groovy/com/athaydes/automaton/samples/SwingJavaFXSampleAppTest.groovy)
I've created myself for testing the *Automaton*, written in Groovy.

But you're probably interested in knowing how you'll be able to test such a mixed application!
Easily, with the `SwingerFXer` *Automaton* driver:

```java
String swingTextAreaText = "Hello, I am Swing...";
String fxInputText = "Hello, JavaFX...";

SwingerFxer swfx = SwingerFxer.getUserWith( swingFrame, fxNode );

swfx.doubleClickOn( "text:colors" )
    .clickOn( "text-area" )
    .type( swingTextAreaText ).pause( 1000 )
    .clickOn( "#left-color-picker" ).pause( 1000 )
    .moveBy( 60, 40 ).click().pause( 1000 )
    .clickOn( "#fx-input" )
    .type( fxInputText );
```

Assertions work for both Swing and JavaFX seamlessly.

```java
assertThat( swfx.getAt( "text-area" ), hasText( swingTextAreaText ) );
assertThat( swfx.getAt( "#fx-input" ), hasText( fxInputText ) );
assertThat( swfx.getAt( "#left-color-picker" ), hasValue( swingJavaFx.getTextLeftColor() ) );
```

The above is a based on the (SwingJavaFXSampleAppTestInJava)[https://github.com/renatoathaydes/Automaton/blob/master/src/test/java/com/athaydes/automaton/samples/SwingJavaFXSampleAppTestInJava.java] sample class in this project.
Just type `t` in the GibHub main page and search for this class for the complete code.

Notice the the `SwingerFxer` is a composite of `Swinger` and `FXer` and therefore contains all their methods.
String selectors work like this:

* if the selector starts with a `.` or `#`, the lookup is made in the JavaFX app.
* in all other cases, the lookup re-directed to the Swinger (which can use built-in and custom selectors).

Pretty simple!

If you want to lookup a JavaFX Node, you'll use a css selector (starting with `.` = by css class, `#` by ID).
To lookup a Swing Component, just use the `Swinger` selector syntax (see `Swinger` section).

## Platform-independent tests

You can test or automate anything that a user can interact with in a computer using *Automaton*.
This is the simplest possible usage, but also the most limited:

```java
Automaton.getUser().moveTo( 0, 0 ).moveBy( 50, 100 ).click()
         .moveBy( 30, 0 ).rightClick().moveBy( 30, 50 )
         .click().dragBy( 50, 100, Speed.SLOW )
         .pause( 1000 ).type( "Automaton" ).type( KeyEvent.VK_ENTER );
```

All other drivers sub-class the Automaton, so they will all inherit its methods.


# Providing custom Configuration

The `Automaton` does not require any external configuration, but it allows the user to provide a configuration file which will be used if found.

To provide custom configuration, simply add a file called `/automaton-config.properties` to the classpath.

The config file contains the following properties:

```properties
# Set the DEFAULT speed to be used by the Automaton when no speed is passed in a method call
# Options are the values of enum: com.athaydes.automaton.Speed
automaton.speed=VERY_FAST
```

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
