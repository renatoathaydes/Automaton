doubleClickOn 'text:colors'
clickOn 'text:yellow'
waitForFxEvents()

assertThat( fxer['status-label'], hasText( 'You selected [colors, yellow]' ) )

println "All tests passed - OK"

pause 3000 // milli-seconds

clickOn 'text:File'
moveBy( 20, 20 )
click() // close window
