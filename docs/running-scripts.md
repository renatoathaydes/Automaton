# Running Automaton Groovy scripts

The easiest way to use Automaton is by creating [Groovy](http://groovy.codehaus.org/) scripts.

It is really simple and does not require any modification to your Java applications!

The following command shows how you would run your Automaton script, called `my-script.groovy`,
to test an application `my-app.jar`:

```
java -javaagent:Automaton-1.x-all-deps.jar=my-script.groovy -jar my-app.jar
```

This mechanism to run scripts is currently limited to Swing application, but soon there will be support for JavaFX applications as well.

Script code can call all `Swinger` methods directly, such as in:

```groovy
clickOn 'text:A label'
```

An instance of `Swinger` called `swinger` is available for the script as well, so you can use it as in this example:

```groovy
import javax.swing.JLabel

JLabel firstLabel = swinger[ JLabel ]
```

You can also call any static methods from the following classes:

  * `org.junit.Assert`
  * `org.hamcrest.CoreMatchers`
  * `com.athaydes.automaton.assertion.AutomatonMatcher`
  * `com.athaydes.automaton.SwingUtil`

The following is a valid example of an Automaton script:

```groovy
doubleClickOn 'text:colors'
pause 500
clickOn 'text:yellow'

assertThat swinger[ 'status-label' ],
           hasText( 'You selected [colors, yellow]' )
```

Scripts are written in Groovy, so you can use the Groovy syntax as required:

```groovy
5.times { index ->
  clickOn "input-$index"
  moveBy( -100, 10 )
  if ( index < 4 )
    type "Hello $index"
}
```
