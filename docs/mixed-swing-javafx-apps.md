
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
    .type( swingTextAreaText )
    .clickOn( "#left-color-picker" ).waitForFxEvents()
    .moveBy( 60, 40 ).click().waitForFxEvents()
    .clickOn( "#fx-input" )
    .type( fxInputText );
```

Assertions work for both Swing and JavaFX seamlessly.

```java
assertThat( swfx.getAt( "text-area" ), hasText( swingTextAreaText ) );
assertThat( swfx.getAt( "#fx-input" ), hasText( fxInputText ) );
assertThat( swfx.getAt( "#left-color-picker" ), hasValue( swingJavaFx.getTextLeftColor() ) );
```

The above is a based on the [SwingJavaFXSampleAppTestInJava](https://github.com/renatoathaydes/Automaton/blob/master/src/test/java/com/athaydes/automaton/samples/SwingJavaFXSampleAppTestInJava.java) sample class in this project.

Notice the the `SwingerFxer` is a composite of `Swinger` and `FXer` and therefore contains all their methods.
String selectors work like this:

* if the selector can be inferred to be specific to JavaFX or Swing (eg. you use a sub-class of `Node` as a selector,
  which must be a JavaFX type), then `Automaton` will look only into the hierarchy of the appropriate framework.
* otherwise, the `Automaton` will look into the JavaFX application hierarchy first.
* if not found in JavaFX, `Automaton` searches the Swing components.

If you want to ensure `Automaton` only looks in either JavaFX or Swing, you can do it like this:

```java
// use the Swinger directly
swfx.getSwinger().getAt( "swing-component" );

// use the FXer directly
swfx.getFxer().getAt( "#javafx-node" );
```

Complex selectors work seamlessly between JavaFX and Swing.
