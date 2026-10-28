package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.junit.jupiter.api.Test;

class SlashCommandsSetUpTest
{
	/** The builder has no public name getter; read it from the JSON body Javacord would send. */
	private static String name(SlashCommandBuilder b)
	{
		try
		{
			Object delegate = b.getDelegate();
			Object json = delegate.getClass().getMethod("getJsonBodyForApplicationCommand").invoke(delegate);
			return json.getClass().getMethod("get", String.class).invoke(json, "name").toString().replace("\"", "");
		}
		catch (ReflectiveOperationException e)
		{
			throw new AssertionError(e);
		}
	}

	private static Set<String> names(Set<SlashCommandBuilder> builders)
	{
		return builders.stream().map(SlashCommandsSetUpTest::name).collect(Collectors.toSet());
	}

	@Test
	void globalSetHoldsOnlyActivestreams()
	{
		assertEquals(Set.of("activestreams"), names(new SlashCommandsSetUp().getCommands()));
	}

	@Test
	void perServerSetHoldsAllSetupCommands()
	{
		assertEquals(Set.of("setupreact", "setupstatus", "setupcontentqueue", "setupannouncements"),
				names(new SlashCommandsSetUp().getSetupCommands()));
	}
}
