# Swing Automaton Cheat-sheet

### Creating a *Swinger*
```java
Swinger swinger = Swinger.getUserWith( topLevelComponent );
```
Or, if you do not have a top-level component:
```java
// will use the first Swing Window returned by java.awt.Window.getWindows() as the topLevelComponent
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

### Writing custom Swinger Selectors

#### Adding custom selectors in Java:
```java
Map<String, Closure<Component>> customSelectors = new LinkedHashMap<>();
customSelectors.put( "cust:", new SwingerSelector( this ) {
    @Override
    public Component call( String selector, Component component ) {
        // return a component based on the given selector and component
    }
} );
swinger.setSpecialPrefixes( customSelectors );
```
To keep the `Automaton` selectors working, make sure to re-add the default selectors to the map:

```java
customSelectors.putAll( Swinger.getDEFAULT_PREFIX_MAP() );
```
Complete sample code at `SwingerSample.java`

#### Example custom selector in Java:
```java
// a custom selector to find things by ID
customSelectors.put( "$", new SwingerSelector( this ) {
    List<Component> found = new ArrayList<Component>( 1 );
    @Override
    public Component call( final String selector, Component component ) {
        SwingUtil.navigateBreadthFirst( component, new Closure( this ) {
            @Override
            public Object call( Object... args ) {
                if ( SwingUtil.callMethodIfExists( args[ 0 ], "getId" ).equals( selector ) ) {
                    found.add( ( Component ) args[ 0 ] );
                }
                return !found.isEmpty();
            }
        } );
        return found.isEmpty() ? null : found.get( 0 );
    }
}
```

#### Adding custom selectors in  Groovy:
```groovy
def customSelectors = [ "cust:": { String selector, Component component ->
        // return a component based on the given selector and component
} ]
swinger.specialPrefixes = Swinger.DEFAULT_PREFIX_MAP + customSelectors
```

Complete sample code at `SwingJavaFXSampleAppTest.groovy`

#### Example custom selector in Groovy
```groovy
// a custom selector to find things by ID
customSelectors[ "$" ] = { String selector, Component component ->
    Component result = null
    SwingUtil.navigateBreadthFirst( component ) {
        if ( SwingUtil.callMethodIfExists( it, "getId" ) == selector )
            result = it
        result != null
    }
    result
} )
```

### Finding Components

#### By name
```java
Component c = swinger.getAt( "component-name" );
```

#### By type
```java
Component c = swinger.getAt( "type:ComponentType" );
```
```java
Component c = swinger.getAt( "type:complete.path.ComponentType" );
```


#### By text
```java
Component c = swinger.getAt( "text:A Component Text" );
```

#### Opening nodes in a JTree
```java
Swinger swinger = Swinger.getUserWith( frame );

JTree tree = ( JTree ) swinger.getAt( "mboxTree" );
List<Component> nodes = SwingUtil.collectNodes( tree, "colors", "red" );

for ( Component node : nodes ) {
    swinger.doubleClickOn( node ).pause( 250 );
}
```