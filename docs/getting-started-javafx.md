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
