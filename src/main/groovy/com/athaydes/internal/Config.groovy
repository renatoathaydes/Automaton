package com.athaydes.internal

import com.athaydes.automaton.Speed
import groovy.util.logging.Slf4j

import static com.athaydes.automaton.Speed.FAST

/**
 *
 * User: Renato
 */
@Singleton
@Slf4j
class Config {

	def resourceLoader = new RealResourceLoader()
	final props = new Properties();

	static final DEFAULT_SPEED = FAST

	Speed getSpeed( ) {
		def customSpeed = null
		try {
			def configFile = resourceLoader.configFile
			if ( configFile?.exists() ) {
				customSpeed = loadSpeedFrom configFile
			}
		} catch ( e ) {
			log.warn "Unable to read Automaton config file", e
		}
		customSpeed ?: DEFAULT_SPEED
	}

	private Speed loadSpeedFrom( File configFile ) {
		String configSpeed = null
		try {
			props.load( configFile.newInputStream() )
			configSpeed = props.getProperty( 'automaton.speed' ).toUpperCase()
			log.info "Config speed loaded from ${configFile.absolutePath}: $configSpeed"
			return configSpeed ? configSpeed as Speed : null
		} catch ( e ) {
			e.printStackTrace()
			log.warn( "Parameter automaton.speed value '${configSpeed}' not valid", e )
		}
	}

}

class RealResourceLoader {

	static final CUSTOM_CONFIG_FILE_LOCATION = "/automaton-config.properties"

	File getConfigFile( ) {
		def resource = this.class.getResource( CUSTOM_CONFIG_FILE_LOCATION )
		if ( resource )
			new File( resource.toURI() )
	}

}
