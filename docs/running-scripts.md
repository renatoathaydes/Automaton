# Running Automaton Groovy scripts

The easiest way to use Automaton is by creating Automaton [Groovy](http://groovy.codehaus.org/) scripts (or AScript).

It is really simple and does not require any modification to your Java applications!

The following command shows how you would run your AScript, called `myAScript.groovy`,
to test an application `my-app.jar`:

```
java -javaagent:Automaton-1.x-all-deps.jar=myAScript.groovy -jar my-app.jar
```

You can also run several scripts in sequence by placing them all in the same directory and giving the location
of that directory to Automaton, rather than just one script.

For example, if you saved your scripts in a directory called `mydir`, you would run your scripts as follows:

```
java -javaagent:Automaton-1.x-all-deps.jar=mydir -jar my-app.jar
```

Automaton will only run the files with the `.groovy` extension under the given directory.
The scripts will be run in alphabetical order.

# Writing Automaton scripts (AScripts)

Writing an AScript is extremely easy, as the following example demonstrates:

```groovy
clickOn 'some-id'
doubleClickOn 'text:Click here'
assertThat swinger[ 'some-label' ], hasText( 'Expected text' )
```

Automaton makes the appropriate driver for your application available automatically, depending on whether
it can detect a Swing JFrame, JavaFX Stage or both.

The driver you get could be one or more of the following:

  * `swinger ` for Swing applications.
  * `fxer` for JavaFX applications.
  * `sfxer` for JavaFX/Swing mixed applications.

In the above example, the script refers to `swinger`, which is the Swing driver.
If your application were an JavaFX app, you could just use the `fxer` driver:

```groovy
// with the JavaFX driver - fxer
assertThat fxer[ 'some-label' ], hasText( 'Expected text' )
```

If your application is a mixed Swing/JavaFX app, you could use the `sfxer` or any of the other drivers.

This only matters when you need to use a driver directly, as in the example `assert`s above.
Most of the time, however, you don't need to use a driver directly and can just call the common
Automaton methods, such as `clickOn`, `doubleClickOn`, `rightClickOn`, `moveTo`, `drag`, `type`, etc.

Another example of using a driver directly is when you want to get a reference to a GUI item using the Groovy
syntax for dictionaries (which translates to the `getAt` method).

For example, suppose you want to find a JTree in your Swing app. You could do one of these:

```groovy
import javax.swing.JLabel

JLabel firstLabel = swinger[ JLabel ]
// or alternatively
firstLabel = getAt( JLabel )
```

The first option can be more efficient because you are effectively telling Automaton that you're looking for an item which
is in the Swing tree, so it won't have to try the JavaFX Nodes as well.

From an AScript, you can also call any static methods from the following classes:

  * `org.junit.Assert`
  * `org.hamcrest.CoreMatchers`
  * `com.athaydes.automaton.assertion.AutomatonMatcher`
  * `com.athaydes.automaton.SwingUtil`

The following is a valid example of an AScript:

```groovy
doubleClickOn 'text:colors'
pause 500
clickOn 'text:yellow'

assertThat swinger[ 'status-label' ],
           hasText( 'You selected [colors, yellow]' )
```

AScripts are written in Groovy, so you can do anything that any Groovy code can:

```groovy
5.times { index ->
  clickOn "input-$index"
  moveBy( -100, 10 )
  if ( index < 4 )
    type "Hello $index"
}
```

## Code highlighting and assistance

You can write your AScripts with code-completion and highlighting with [IntelliJ IDEA](https://www.jetbrains.com/idea/).

To enable AScript support, all you have to do is add Automaton to your project's class-path. IntelliJ will then automatically
recognize any file whose name matches `*AScript.groovy` as being an Automaton script. For example:

 * LoginAScript.groovy
 * SomeTestAScript.groovy
 * AScript.groovy

Writing AScripts in IntelliJ is a pleasure:

![auto-completion in IntelliJ](images/code-completion.png)

Notice that you won't be able to run the script as you would run a normal Groovy script in IntelliJ.
