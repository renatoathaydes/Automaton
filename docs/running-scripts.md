# Running Automaton Groovy scripts

The easiest way to use Automaton is by creating scripts.

It is really simple and does not require any modification to your Java applications!

The following command shows how you would run your Automaton script, called `my-script.groovy`,
to test an application `my-app.jar`:

```
java -javaagent:Automaton-1.x-all-deps.jar=my-script.groovy -jar my-app.jar
```

This mechanism to run scripts is currently limited to Swing application, but soon there will be support for JavaFX applications as well.
