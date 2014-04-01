# Getting Started with Swing

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
swinger.clickOn( "text-input-1" )
       .type( "Hello Automaton!" )
       .drag( "text:Drag this item" )
       .onto( matchingAll( "type:DropBoxImpl", "text:Drop here!" ) );

// get the tree nodes for the given tree path and open them
JTree myTree = swinger.getAt( JTree.class );
List<Component> nodesToOpen = SwingUtil.collectNodes( myTree,
                 "Project 1", "Test Suite A", "Test Case 1" );
swinger.doubleClickOn( nodesToOpen ); // open the Tree Nodes
```

Built-in Swing selectors:

  * `name:` select by name (the default, so no prefix is required).
    * Example: `swinger.clickOn( "name:my-component" );`
  * `text:` select by text (works for anything that has a label, `JTable`'s cells and `JTree`'s nodes.
    * Example: `swinger.clickOn( "text:My Component" );`
  * `type:` select by type. Use the class's simple or qualified name.
    * Example: `swinger.clickOn( "type:JButton" );`

You can also select a Component by type, type-safely, as follows:

```java
JButton button = fxer.getAt( JButton.class );
// or to get the second button (index starts from 0)
JButton button = fxer.getAll( JButton.class ).get( 1 );
```

Building complex selectors:

```java
import static com.athaydes.automaton.selector.StringSelectors.matchingAll;

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

