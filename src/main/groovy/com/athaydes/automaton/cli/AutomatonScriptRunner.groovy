package com.athaydes.automaton.cli

import com.athaydes.automaton.FXApp
import com.athaydes.automaton.FXer
import com.athaydes.automaton.GuiItemNotFound
import com.athaydes.automaton.SwingUtil
import com.athaydes.automaton.Swinger
import com.athaydes.automaton.SwingerFxer
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
            sysoutInterceptor = new SystemOutputInterceptor( { s -> writeSafely( writer, s ) }, false )
            syserrInterceptor = new SystemOutputInterceptor( { s -> writeSafely( writer, s ) }, true )
            sysoutInterceptor.start()
            syserrInterceptor.start()
        }

        def shell = new GroovyShell( this.class.classLoader, BindingProviderBridge.instance.provideBinding(), config )
        try {
            shell.evaluate( text )
        } catch ( e ) {
            System.err.println( e )
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
class BindingProviderBridge {

    Binding provideBinding() {
        Class.forName( javaFXPresent() ?
                'com.athaydes.automaton.cli.SwingJavaFXBindingProvider' :
                'com.athaydes.automaton.cli.SwingOnlyBindingProvider' )
                .newInstance().provideBinding()
    }

    private boolean javaFXPresent() {
        try {
            Class.forName( 'javafx.application.Application' )
            true
        } catch ( ClassNotFoundException e ) {
            false
        }
    }

}

class SwingJavaFXBindingProvider {

    Binding provideBinding() {
        def swinger = attemptToGetSwinger()
        def fxer = FXApp.initialized ? FXer.getUserWith( FXApp.scene.root ) : null
        def sfxer = ( swinger && fxer ) ? SwingerFxer.getUserWith( swinger.root, fxer.root ) : null
        if ( !swinger && !fxer ) {
            throw new RuntimeException( 'Could not find any Swing window or JavaFX Stage - cannot run AScript' )
        }
        new Binding( swinger: swinger, fxer: fxer, sfxer: sfxer )
    }

    Swinger attemptToGetSwinger() {
        try {
            return Swinger.forSwingWindow()
        } catch ( GuiItemNotFound e ) {
            return null
        }
    }

}

class SwingOnlyBindingProvider {

    Binding provideBinding() {
        new Binding( swinger: Swinger.forSwingWindow(), fxer: null, sfxer: null )
    }

}
