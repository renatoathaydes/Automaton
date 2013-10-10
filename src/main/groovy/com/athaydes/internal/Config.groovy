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
	def props = new Properties();

	Speed getSpeed( ) {
		def defaultSpeed = FAST
		def configFile = resourceLoader.configFile
		def customSpeed = null
		if ( configFile?.exists() ) {
			customSpeed = loadSpeedFrom configFile
		}
		customSpeed ?: defaultSpeed
	}

	private Speed loadSpeedFrom( File configFile ) {
		props.load( configFile.newInputStream() )
		String configSpeed = null
		try {
			configSpeed = props.getProperty( 'automaton.speed' ).toUpperCase()
			println "Config speed: $configSpeed"
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
