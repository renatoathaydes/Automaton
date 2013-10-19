package com.athaydes.automaton.internal

import com.athaydes.internal.Config
import com.athaydes.internal.RealResourceLoader
import org.junit.Test

import static com.athaydes.automaton.Speed.SLOW

/**
 *
 * User: Renato
 */
class ConfigTest {

	Config config = Config.instance

	@Test
	void "Provides default SPEED if no config file exists"( ) {
		config.resourceLoader = [ getConfigFile: { new File( 'DOES_NOT_EXIST' ) } ] as RealResourceLoader
		assert config.speed == Config.DEFAULT_SPEED
	}

	@Test
	void "Provides default SPEED if config file specifies invalid speed"( ) {
		def tempFile = configFileWith( "automaton.speed=INVALID" )

		config.resourceLoader = [ getConfigFile: { tempFile } ] as RealResourceLoader

		assert config.speed == Config.DEFAULT_SPEED
	}

	@Test
	void "Provides default SPEED if config file does not specify a speed"( ) {
		def tempFile = configFileWith( "automaton.something=else\na.prop=hi" )

		config.resourceLoader = [ getConfigFile: { tempFile } ] as RealResourceLoader

		assert config.speed == Config.DEFAULT_SPEED
	}

	@Test
	void "Provides default SPEED if config file cannot be read by any reason"( ) {
		config.resourceLoader = [ getConfigFile: { throw new Exception() } ] as RealResourceLoader

		assert config.speed == Config.DEFAULT_SPEED
	}

	@Test
	void "Provides SPEED set by config file if valid"( ) {
		def tempFile = configFileWith( "automaton.speed=${SLOW.name()}" )

		config.resourceLoader = [ getConfigFile: { tempFile } ] as RealResourceLoader

		assert config.speed != null
		assert config.speed == SLOW
	}

	private File configFileWith( String text ) {
		def tempFile = File.createTempFile( 'temp-config', '.properties' )
		tempFile.write( text )
		tempFile
	}

}
