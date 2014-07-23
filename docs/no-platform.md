# Platform-independent tests

You can test or automate anything that a user can interact with in a computer using *Automaton*.
This is the simplest possible usage, but also the most limited:

```java
Automaton.getUser().moveTo( 0, 0 ).moveBy( 50, 100 ).click()
         .moveBy( 30, 0 ).rightClick().moveBy( 30, 50 )
         .click().dragBy( 50, 100, Speed.SLOW )
         .pause( 1000 ).type( "Automaton" ).type( KeyEvent.VK_ENTER );
```

All other drivers sub-class the Automaton, so they will all inherit its methods.

Currently, the only way to interact with platform-independent UIs is by using the screen coordinates of elements,
as shown above. However, it is possible that in the future more advanced means will be added to allow users to
test/automate any UI.

## Typing unicode characters

A problem some users may face is that, because Automaton actually simulates keyboard events when typing, it is very
difficult to support typing unicode characters correctly.

So if you try to enter an URL in a text field, for example, it may not work depending on your keyboard layout:

```groovy
import static java.awt.event.KeyEvent.*

Automaton.user.type( 'http://localhost:8080/examples' ).type( VK_ENTER )
```

The above should work on English keyboards, but it is not likely to work with any other layouts.

**The preferred method to enter special characters is to use one of the UI-specific driver methods, such as ``enterText``.**

Another work-around is to use the Java class [``KeyEvent``](http://docs.oracle.com/javase/7/docs/api/java/awt/event/KeyEvent.html), which Automaton supports:

```groovy
import static java.awt.event.KeyEvent.*

Automaton.user.type('http').pressSimultaneously( VK_SHIFT, VK_COLON )
				.type( '//localhost' ).pressSimultaneously( VK_SHIFT, VK_COLON )
				.type( '8099' ).pause( 100 )

assert jta.text == 'http://localhost:8099'
```

Notice that the above code uses the ``pressSimultaneously`` method, which allows you to enter any combination of keys
together, just like you would in a real keyboard.

So, to enter the word ``coração`` with a Brazilian keyboard layout (at least if you're on an actual English keyboard,
like me), you could try this:

```groovy
Automaton.user.type('cora').type( VK_COLON )
	.pressSimultaneously( VK_QUOTE, VK_A ).type( 'o' )
assert jta.text == 'coração'
```
