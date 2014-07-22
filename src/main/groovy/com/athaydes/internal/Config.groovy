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

	private Config() {
		reload()
	}

	void reload() {
		props.clear()
		try {
			def configFile = resourceLoader.configFile
			if ( configFile?.exists() ) {
				props.load( configFile.newInputStream() )
			} else {
				log.info( "No Automaton config file found, using defaults" )
			}
		} catch ( e ) {
			log.warn( "Unable to load configuration properties", e )
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
		getPropertyValue( 'automaton.interactive', false ) { String configValue ->
			if ( configValue && configValue.toLowerCase() == 'true' ) configValue
		} as boolean
	}

	private getPropertyValue( String key, defaultValue, Closure getValidated ) {
		try {
			def propValue = getValidated( props.getProperty( key ) )
			propValue ?: defaultValue
		} catch ( ignore ) {
			log.warn( "Property ${key} invalid, will use default value" )
			defaultValue
		}
	}

}

class RealResourceLoader {

	static final CUSTOM_CONFIG_FILE_LOCATION = "/automaton-config.properties"

	File getConfigFile() {
		def resource = this.class.getResource( CUSTOM_CONFIG_FILE_LOCATION )
		if ( resource )
			return new File( resource.toURI() )
		null
	}

}
