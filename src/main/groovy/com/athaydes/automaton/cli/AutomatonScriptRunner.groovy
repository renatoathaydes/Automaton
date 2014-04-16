package com.athaydes.automaton.cli

import com.athaydes.automaton.*
import com.athaydes.automaton.assertion.AutomatonMatcher
import groovy.ui.SystemOutputInterceptor
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.hamcrest.CoreMatchers
import org.junit.Assert

/**
 * @author Renato
 */
@Singleton
class AutomatonScriptRunner {

	void run( String fileName, def writer = null ) {
		def file = new File( fileName )
		if ( file.isFile() ) {
			println "Running script $file.absolutePath"
			runScript( file.text, writer )
		} else {
			if ( file.isDirectory() ) {
				println "Looking for groovy scripts under $file.absolutePath"
				def groovyFiles = file.listFiles()?.findAll {
					it.name.endsWith( '.groovy' )
				}?.sort { it.name }?.each { File groovyFile ->
					run( groovyFile.absolutePath, writer )
				}
				if ( !groovyFiles ) println "No groovy scripts found"
			} else println "Cannot find file $fileName"
		}
	}

	void runScript( String text, def writer = null ) {
		def config = new CompilerConfiguration()
		config.scriptBaseClass = AutomatonScriptBase.name

		def imports = new ImportCustomizer().addStaticStars(
				Assert.class.name,
				AutomatonMatcher.class.name,
				CoreMatchers.class.name,
				SwingUtil.class.name )
		config.addCompilationCustomizers( imports )

		def sysoutInterceptor = null
		def syserrInterceptor = null
		if ( writer ) {
			sysoutInterceptor = new SystemOutputInterceptor( { String s -> writer.write( s ); false }, false )
			syserrInterceptor = new SystemOutputInterceptor( { String s -> writer.write( s ); false }, true )
			sysoutInterceptor.start()
			syserrInterceptor.start()
		}

		def swinger = Swinger.swingWindowExists() ? Swinger.forSwingWindow() : null
		def fxer = FXApp.hasInitialized() ? FXer.getUserWith( FXApp.scene.root ) : null

		if ( swinger == null && fxer == null ) {
			throw new RuntimeException( 'Unable to find any Swing or JavaFX applications' )
		}

		def sfxer = ( swinger && fxer ) ? SwingerFxer.getUserWith( swinger.root, fxer.root ) : null

		AutomatonScriptBase.swinger = swinger
		AutomatonScriptBase.fxer = fxer
		AutomatonScriptBase.sfxer = sfxer

		def shell = new GroovyShell( this.class.classLoader,
				new Binding( swinger: swinger, fxer: fxer, sfxer: sfxer ), config )

		try {
			shell.evaluate( text )
		} catch ( Throwable e ) {
			e.printStackTrace()
		} finally {
			sysoutInterceptor?.stop()
			syserrInterceptor?.stop()
		}
	}


}

abstract class AutomatonScriptBase extends Script {

	static swinger
	static fxer
	static sfxer

	def driver

	def methodMissing( String name, def args ) {
		if ( !driver ) {
			driver = sfxer ?: swinger ?: fxer
		}
		try {
			driver."$name"( *args )
		} catch ( MissingMethodException e ) {
			e.printStackTrace()
		}
	}

}

