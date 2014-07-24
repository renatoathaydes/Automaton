package com.athaydes.automaton.cli

import com.athaydes.automaton.FXApp
import com.athaydes.automaton.FXer
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

        def shell = new GroovyShell( this.class.classLoader,
                new Binding( swinger: FXApp.initialized ? null : Swinger.forSwingWindow(),
                        fxer: FXApp.initialized ? FXer.getUserWith( FXApp.scene.root ) : null ), config )
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
            ( swinger ?: fxer )."$name"( *args )
        } catch ( MissingMethodException e ) {
            e.printStackTrace()
        }
    }

}

