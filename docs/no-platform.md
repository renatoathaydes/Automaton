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
