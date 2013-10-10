package com.athaydes.automaton.internal

import com.athaydes.automaton.Speed
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
	void "Can provide the DEFAULT speed if no config file exists"( ) {
		assert config.speed != null
		assert config.speed in Speed.values()
	}

	@Test
	void "Provides SPEED set by config file"( ) {
		def tempFile = File.createTempFile( 'temp-config', '.properties' )
		tempFile.write( "automaton.speed=${SLOW.name()}" )

		config.resourceLoader = [ getConfigFile: { tempFile } ] as RealResourceLoader

		assert config.speed != null
		assert config.speed == SLOW
	}

}
