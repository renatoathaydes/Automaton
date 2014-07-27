package com.athaydes.internal

import com.athaydes.automaton.Speed
import groovy.util.logging.Slf4j

import static com.athaydes.automaton.Speed.FAST

/**
 *
 * User: Renato
 */
@Singleton(strict = false)
@Slf4j
class Config {

	def resourceLoader = new RealResourceLoader()
	final props = new Properties();

	static final DEFAULT_SPEED = FAST

    static final booleanValidator = { String configValue ->
        if ( configValue && isBoolean( configValue ) ) toBoolean( configValue )
    }

    private Config() {
		reload()
	}

	void reload() {
		props.clear()
        def configStream
		try {
            configStream = resourceLoader.config
			if ( configStream ) {
                log.debug 'Will use custom config file'
			    props.load( configStream )
			} else {
				log.info( "No Automaton config file found, using defaults" )
			}
		} catch ( e ) {
			log.warn( "Unable to load configuration properties", e )
		} finally {
            configStream?.close()
        }
	}

	void setResourceLoader( resourceLoader ) {
		this.resourceLoader = resourceLoader
		reload()
	}

	Speed getSpeed() {
		getPropertyValue( 'automaton.speed', DEFAULT_SPEED ) { configValue ->
			if ( configValue ) configValue.toString().toUpperCase() as Speed
		} as Speed
	}

	boolean isInteractiveMode() {
		getPropertyValue( 'automaton.interactive', false, booleanValidator ) as boolean
	}

    boolean isDisableBringStageToFront() {
        getPropertyValue( 'automaton.javafx.disableBringStageToFront', false, booleanValidator ) as boolean
    }

	private getPropertyValue( String key, defaultValue, Closure getValidated ) {
		try {
			def propValue = getValidated( props.getProperty( key ) )
			propValue == null ? defaultValue : propValue
		} catch ( ignore ) {
			log.warn( "Property ${key} invalid, will use default value" )
			defaultValue
		}
	}

    private static boolean isBoolean( String value ) {
        value.trim().toLowerCase() in ['true', 'false']
    }

    private static boolean toBoolean( String configValue ) {
        switch(configValue.trim().toLowerCase()) {
            case 'true': return true
            default: return false
        }
    }

}

@Slf4j
class RealResourceLoader {

	static final CUSTOM_CONFIG_FILE_LOCATION = "/automaton-config.properties"

	InputStream getConfig() {
        def resource = this.class.getResource( CUSTOM_CONFIG_FILE_LOCATION )
        if ( resource ) {
            log.info( "Located custom config at ${resource.toURI()}" )
            return resource.newInputStream()
        }
		null
	}

}
