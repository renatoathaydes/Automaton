package com.athaydes.automaton.cli

import com.athaydes.automaton.SwingUtil
import com.athaydes.automaton.Swinger
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

	void runFile( String fileName, def writer = null ) {
		def file = new File( fileName )
		if ( file.isFile() ) {
			runScript( file.text, writer )
		} else {
			if ( file.isDirectory() ) println "Not a file: $fileName"
			else println "Cannot find file $fileName"
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

		def shell = new GroovyShell( this.class.classLoader,
				new Binding( swinger: AutomatonScriptBase.swinger ), config )
		try {
			shell.evaluate( text )
		} catch ( e ) {
			e.printStackTrace()
		} finally {
			sysoutInterceptor?.stop()
			syserrInterceptor?.stop()
		}
	}


}

abstract class AutomatonScriptBase extends Script {

	static swinger = Swinger.forSwingWindow()

	def methodMissing( String name, def args ) {
		try {
			swinger."$name"( *args )
		} catch ( MissingMethodException e ) {
			e.printStackTrace()
		}
	}

}

