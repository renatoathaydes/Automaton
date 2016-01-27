## AScript Demo

[AScript](/docs/running-scripts.md) is short for Automaton Script, which is a Groovy script that uses
Automaton's DSL for testing.

This demo consists of a little Java UI application with a mixed JavaFX/Swing interface and a few
AScripts that test this application.

### Building the Java UI App

Before you can run the AScripts, you must build the [Java UI App](src/MySwingJavaFXApp.java),
which you can do with the following commands:

```
# create a directory for the compiled class files
mkdir out

# compile
javac -d out src/DemoUI.java
```

### Running the demo Java UI

To make sure everything is working, run the Demo UI:

```
java -cp out DemoUI
```

You should see our demo Java UI App on your screen.

### Running AScripts

> This section assumes you have the Automaton all-deps jar at `../build/libs/automaton-1.3.1-all-deps.jar` relative
  to the working directory. If you don't have the jar yet, download it from JCenter or just build it if you have
  cloned the Automaton repo by running this command from the root directory: `./gradlew uberjar`

You probably noticed that there are some AScripts in the `test` directory!

Now, to run one of them, just run the demo application with this command (with tells java to run the Automaton Java Agent
with the [test/simpleAScript.groovy](test/simpleAScript.groovy) ):

```
# change the paths and version of the automaton jar as appropriate
java -javaagent:../build/libs/automaton-1.3.1-all-deps.jar=test/simpleAScript.groovy -cp out DemoUI
```

Once you enter this command, do not touch the keyboard or mouse because as soon as the Java application starts,
the AScript will start running!

> Notice that if you edit `*AScript.groovy` files in 
 [IntelliJ](https://github.com/renatoathaydes/Automaton/blob/master/docs/images/code-completion.png),
 you'll get auto-completion as long as you have the Automaton jar in the project classpath!

To run all AScripts in the `test` directory, just pass the directory to the Automaton Java Agent instead of a file:

```
java -javaagent:../build/libs/automaton-1.3.1-all-deps.jar=test -cp out DemoUI
```

