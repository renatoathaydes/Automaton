package com.athaydes.automaton.cli

import com.athaydes.automaton.FXer
import com.athaydes.automaton.GuiItemNotFound
import com.athaydes.automaton.Speed
import com.athaydes.automaton.SwingUtil
import com.athaydes.automaton.Swinger
import com.athaydes.automaton.SwingerFxer
import com.athaydes.automaton.assertion.AutomatonMatcher
import groovy.transform.CompileStatic
import groovy.ui.SystemOutputInterceptor
import groovy.util.logging.Slf4j
import javafx.embed.swing.JFXPanel
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.hamcrest.CoreMatchers
import org.junit.Assert

import java.util.concurrent.atomic.AtomicReference

/**
 * @author Renato
 */
@Singleton
class AutomatonScriptRunner {

	@CompileStatic
	void run( String fileName, def writer = null, boolean exitOnScriptEnd = false ) {
		final error = new AtomicReference<Throwable>()
		def file = new File( fileName )

		try {
			if ( file.isFile() ) {
				println "Running script $file.absolutePath"
				runScript( file.text, writer )
				println "AScript finished running without errors"
			} else {
				if ( file.isDirectory() ) {
					println "Looking for groovy scripts under $file.absolutePath"
					def groovyFiles = file.listFiles()?.findAll { File f ->
						f.name.endsWith( '.groovy' )
					}?.sort { it.name }?.each { File groovyFile ->
						run( groovyFile.absolutePath, writer, false )
					}
					if ( !groovyFiles ) println "No groovy scripts found"
				} else println "Cannot find file $fileName"
			}
		} catch ( Throwable e ) {
			println "AScript failed due to $e"
			def element = e.stackTrace.find { it.className =~ /Script\d+/ }
			if ( element ) {
				println "    at Script line ${element.lineNumber}"
			}
			error.set( e )
		}

		if ( exitOnScriptEnd ) {
			Thread.start {
				sleep 500
				if ( error.get() != null ) {
					System.exit( ( error.get() instanceof AssertionError ) ? 10 : 5 )
				} else {
					System.exit( 0 )
				}
			}
		}
	}

	@CompileStatic
	void runScript( String text, def writer = null ) {
		def config = new CompilerConfiguration()
		config.scriptBaseClass = AutomatonScriptBase.name

		def imports = new ImportCustomizer().addStaticStars(
				Assert.getName(), // CompileStatic bug
				AutomatonMatcher.getName(),
				CoreMatchers.getName(),
				SwingUtil.getName(),
				Speed.getName() )

		config.addCompilationCustomizers( imports )

		SystemOutputInterceptor sysoutInterceptor = null
		SystemOutputInterceptor syserrInterceptor = null

		if ( writer ) {
			sysoutInterceptor = new SystemOutputInterceptor( { s -> writeSafely( writer, s ) }, false )
			syserrInterceptor = new SystemOutputInterceptor( { s -> writeSafely( writer, s ) }, true )
			sysoutInterceptor.start()
			syserrInterceptor.start()
		}

		def shell = new GroovyShell( this.class.classLoader, BindingProviderBridge.instance.provideBinding(), config )

		try {
			shell.evaluate( text )
		} catch ( Exception | AssertionError e ) {
			if ( writer ) writeSafely( writer, e )
			throw e
		} finally {
			sysoutInterceptor?.stop()
			syserrInterceptor?.stop()
		}
	}

	boolean writeSafely( writer, s ) {
		try {
			writer.write( s ?: '' )
		} catch ( e ) {
			System.err.println( e )
		}
		return false
	}


}

abstract class AutomatonScriptBase extends Script {

	def methodMissing( String name, def args ) {
		try {
			( sfxer ?: ( swinger ?: fxer ) )."$name"( *args )
		} catch ( MissingMethodException e ) {
			e.printStackTrace()
		}
	}

}

/**
 * A bridge to avoid trying to load JavaFX classes if the system does not have JavaFX at runtime
 */
@Singleton
@Slf4j
class BindingProviderBridge {

	Binding provideBinding() {
		Class.forName( javaFXPresent() ?
				'com.athaydes.automaton.cli.SwingJavaFXBindingProvider' :
				'com.athaydes.automaton.cli.SwingOnlyBindingProvider' )
				.newInstance().provideBinding()
	}

	private static boolean javaFXPresent() {
		try {
			Class.forName( 'javafx.application.Application' )
			true
		} catch ( ClassNotFoundException ignore ) {
			log.warn( 'This JVM instance does not seem to have JavaFX in the classpath! Automaton will only support' +
					' testing Swing Components' )
			false
		}
	}

}

@SuppressWarnings( "GroovyUnusedDeclaration" )
@Slf4j
class SwingJavaFXBindingProvider {

	Binding provideBinding() {
		def swinger = attemptToGetSwinger()
		def fxer = attemptToGetFXer swinger

		if ( !swinger && !fxer ) {
			throw new RuntimeException( "Impossible to run AScript! No Swing Window or JavaFX Scene found in" +
					" the current JVM instance. Please make sure to run your UI Application in the same JVM" +
					" instance as Automaton." )
		}

		def sfxer = ( swinger && fxer ) ? SwingerFxer.getUserWith( swinger.root, fxer.root ) : null

		new Binding( swinger: swinger, fxer: fxer, sfxer: sfxer )
	}

	Swinger attemptToGetSwinger() {
		try {
			return Swinger.forSwingWindow()
		} catch ( IllegalArgumentException ignore ) {
			log.warn( 'Cannot find a running Swing Window, AScript will not be able to use a Swing driver' )
			return null
		}
	}

	FXer attemptToGetFXer( Swinger swinger ) {
		try {
			return FXer.getUserWith()
		} catch ( IllegalArgumentException ignore ) {
			if ( swinger ) {
				try {
					def jfxPanel = swinger[ JFXPanel ]
					return FXer.getUserWith( jfxPanel.scene.root )
				} catch ( GuiItemNotFound ignored ) {
					log.warn( 'Cannot find a running JavaFX Stage, AScript will not be able to use a JavaFX driver' )
				}
			}
			return null
		}
	}

}

@SuppressWarnings( "GroovyUnusedDeclaration" )
class SwingOnlyBindingProvider {

	Binding provideBinding() {
		new Binding( swinger: Swinger.forSwingWindow(), fxer: null, sfxer: null )
	}

}
