package org.example.web;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Log4jConfigTest
{
	@Test
	void writesBotLogUnderConfigDir() throws Exception
	{
		String xml = Files.readString(Path.of("src/main/resources/log4j2.xml"));
		assertTrue(xml.contains("${env:CONFIG_DIR:-/config}/logs"));
		assertTrue(xml.contains("${LOG_DIR}/bot.log"));
		assertTrue(xml.contains("SizeBasedTriggeringPolicy"));
	}
}
