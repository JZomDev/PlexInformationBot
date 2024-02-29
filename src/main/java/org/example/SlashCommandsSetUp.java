package org.example;

import java.util.HashSet;
import java.util.Set;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.SlashCommandBuilder;

public class SlashCommandsSetUp
{
	public Set<SlashCommandBuilder> getCommands()
	{

		Set<SlashCommandBuilder> builders = new HashSet<>();

		builders.add(new SlashCommandBuilder().setName("activestreams")
			.setDescription("Stream count")
			.setDefaultEnabledForPermissions(PermissionType.ADMINISTRATOR)
		);

		return builders;
	}
}
