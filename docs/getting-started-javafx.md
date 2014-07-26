## JavaFX applications

### Starting:

Automaton has several options you can choose from to start running tests on your app:

#### Let Automaton start your app

You can simply let the `Automaton` start your app:

```java
FXApp.startApp( new MyApp() );
FXer fxer = FXer.getUserWith( FXApp.getScene().getRoot() );
```

#### Launch your app then give Automaton a Node

Or you can launch it using your own launcher, then create a FXer istance from any node at any time:

```java
FXer fxer = FXer.getUserWith( node );
```

To just test a Node and its internals (eg. your custom components unit test), you can do this:

```java
final Stage stage = FXApp.initialize();
final CustomNode customNode = new CustomNode();
FXApp.doInFXThreadBlocking( new Runnable() {
  public void run() {
    stage.getScene().setRoot( customNode );
  }
} );
FXer fxer = FXer.getUserWith( customNode );
```

#### Let Automaton find your Stage

This option allows you to run tests on an application even if you cannot change the way it is started and cannot
directly get a reference to any Node.

The only requirement is that the Automaton tests run in the same Java Virtual Machine as your application.

```java
FXApp.initializeIfStageExists();
if (FXApp.isInitialized()) {
  FXer fxer = FXer.getUserWith( FXApp.getScene().getRoot() );
  // start testing!
  
} else {
  throw new RuntimeException( "Could not find a JavaFX Stage" );
}
```

If you decide to use this option, you might also want to have a look at running your tests as [AScripts](running-scripts.md)
rather than Java tests.

### Testing with Automaton

Emulating user's actions in the GUI:
```java
fxer.clickOn( TextField.class )
    .type( "Automaton" )
    .clickOn( "#login-button" ).waitForFxEvents();
```

Built-in JavaFX selectors:

  * `#` select by ID (the default, so no prefix is required).
    * Example: `fxer.clickOn( "#my-node" );`
  * `.` select by css class.
    * Example: `fxer.clickOn( ".invalid" );`
  * `text:` select by text (works for anything that has a label).
    * Example: `fxer.clickOn( "text:My Node" );`
  * `type:` select by type. Use the class's simple or qualified name.
    * Example: `fxer.clickOn( "type:TextArea" );`

You can also select a Node by type, type-safely, as follows:

```java
VBox vbox = fxer.getAt( VBox.class );
// or to get the second VBox (index starts from 0)
VBox vbox = fxer.getAll( VBox.class ).get( 1 );
```

Building complex selectors:

```java
import static com.athaydes.automaton.selector.StringSelectors.matchingAll;

swinger.clickOn( matchingAll( "type:MyDraggable", "text:Drag this item" ) );
```

Making assertions with `Automaton`'s Hamcrest matchers:

```java
assertThat( fxer.getAt( "#message-area" ), hasText( "Please enter your password" ) );
```

Creating your own selectors (the example below adds a selector which can find nodes
whose style String contains a certain sub-string):

```java
Map<String, AutomatonSelector<Node>> customSelectors = new HashMap<>();

customSelectors.put( "$", new SimpleFxSelector() {
  @Override
  public boolean followPopups() {
    return false;
  }

  @Override
  public boolean matches( String selector, Node node ) {
    return node.getStyle().contains( selector );
  }
} );
customSelectors.putAll( FXer.getDEFAULT_SELECTORS() );

fxer.setSelectors( customSelectors );

Node blueNode = fxer.getAt( "$blue" );
```
