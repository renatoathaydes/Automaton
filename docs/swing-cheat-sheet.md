# Swing Automaton Cheat-sheet

### Creating a *Swinger*
```java
Swinger swinger = Swinger.getUserWith( topLevelComponent );
```
Or, if you do not have a top-level component available:
```java
// will use the first JFrame returned by java.awt.Window.getWindows() as the topLevelComponent
Swinger swinger = Swinger.forSwingWindow();
```

### Default Swinger Selectors
All examples use the `clickOn` method, but any other `Swinger` method that takes a String selector could be used

#### By name

```java
swinger.clickOn( "component-name" );
```
Qualified usage:
```java
swinger.clickOn( "name:component-name" );
```

#### By type

```java
swinger.clickOn( "type:JButton" )
```
```java
swinger.clickOn( "type:javax.swing.Box" )
```

#### By text

```java
swinger.clickOn( "text:My Button's Label" )
```
The **text** selector also works with `JTree` and `JTable`.
```java
swinger.clickOn( "text:A JTree Node" )
       .clickOn( "text:A JTable Header or Cell" )
```

### Finding Components

**Note:**

The `getAt` throws a `RuntimeException` if nothing is found.

`getAll` returns all components matching the selector, or an empty list if none is found.

#### By name
```java
Component c = swinger.getAt( "component-name" );
```
```java
List<Component> cs = swinger.getAll( "component-name" );
```

#### By type
```java
JButton b = swinger.getAt( JButton.class );
```

```java
Component c = swinger.getAt( "type:ComponentType" );
```
```java
Component c = swinger.getAt( "type:complete.path.ComponentType" );
```
```java
List<JButton> cs = swinger.getAll( JButton.class );
```

#### By text
```java
Component c = swinger.getAt( "text:A Component Text" );
```
```java
List<Component> cs = swinger.getAll( "text:Some Components Text" );
```

#### Using Complex matchers

Matching any:
```java
import static com.athaydes.automaton.selector.StringSelectors.matchingAny;

// throws an Exception if nothing is found
Component c = swinger.getAt( matchingAny( "text:colors", "text:sports" ) );

// finds all components matching the selector, or an empty list if nothing is found
List<Component> nodes = swinger.getAll( matchingAny( "text:colors", "text:sports" ) );

// uses `getAt` to find the component to clickOn
swinger.clickOn( matchingAny( "text:colors", "text:sports" ) );
```

Matching all:
```java
import static com.athaydes.automaton.selector.StringSelectors.matchingAll;

// throws an Exception if nothing is found
Component c = swinger.getAt( matchingAll( "type:JButton", "text:sports" ) );

// finds all components matching the selector, or an empty list if nothing is found
List<Component> nodes = swinger.getAll( matchingAll( "type:JButton", "text:sports" ) );

// uses `getAt` to find the component to clickOn
swinger.clickOn( matchingAll( "type:JButton", "text:sports" ) );
```

#### Opening nodes in a JTree
```java
Swinger swinger = Swinger.getUserWith( frame );

JTree tree = swinger.getAt( JTree.class );
List<Component> nodes = SwingUtil.collectNodes( tree, "colors", "red" );
swinger.doubleClickOn( nodes );
```

### Automaton Matchers

#### hasText
Works the same way as the `byText` selector.
```java
import static com.athaydes.automaton.assertion.AutomatonMatcher.hasText

assertThat( myComponent, hasText( "Some text" ) );
```
#### hasValue
Extends `hasText` to also work with anything that might have a value (uses the following methods to find a Component's value: `getValue`, `getText`,	`getSelected`, `getColor`).
```java
import static com.athaydes.automaton.assertion.AutomatonMatcher.hasValue

assertThat( myComponent, hasValue( "Some text" ) );

assertThat( myColorPicker, hasValue( Color.BLUE ) );
```

#### selected
Check if a component is selected (usually used with the Hamcrest `is` matcher).
```java
import static com.athaydes.automaton.assertion.AutomatonMatcher.selected

assertThat( myComponent, is( selected() ) );
```

#### visible
Check if a component is visible (usually used with the Hamcrest `is` matcher) as reported by the component's `isVisible` method. This is NOT the same as `showing` (see below).
```java
import static com.athaydes.automaton.assertion.AutomatonMatcher.visible

assertThat( myComponent, is( visible() ) );
```


#### showing
Check if a component is showing on the screen (usually used with the Hamcrest `is` matcher) as reported by the component `isShowing` method.
```java
import static com.athaydes.automaton.assertion.AutomatonMatcher.showing

assertThat( myComponent, is( showing() ) );
```

### For Groovy users

You may have noticed that the `Automaton` intentionally uses the method `getAt`, which has special syntax in Groovy, to find elements. So anywhere you see `getAt`, you can use instead the `[]` operator:

```groovy
// by type
JButton b = swinger[ JButton ]

// by name
def c = swinger[ 'component-name' ]

// by text
def d = swinger[ 'text:Something' ]

// complex selectors
def e = swinger[ matchingAny( 'click-me', 'text:Click me' ) ]

def f = swinger[ matchingAll( 'type:JButton', 'text:Click me' ) ]
```