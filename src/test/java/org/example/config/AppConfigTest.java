package org.example.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConfigTest
{
	@TempDir
	Path tmp;

	@Test
	void getReturnsFileOverEnvAndReloadSeesNewFile() throws IOException
	{
		SettingsStore store = new SettingsStore(tmp, Map.of("IP", "1.1.1.1", "PORT", "32400"));
		AppConfig.init(store);
		assertEquals("1.1.1.1", AppConfig.get().plexIp());

		store.save(new Settings("", "2.2.2.2", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
		AppConfig.reload();

		assertEquals("2.2.2.2", AppConfig.get().plexIp());
		assertEquals("32400", AppConfig.get().plexPort());
	}
}
