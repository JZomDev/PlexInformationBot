package org.example;

import java.util.HashSet;
import java.util.Set;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;

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

	/** Per-server admin commands; never register these globally. */
	public Set<SlashCommandBuilder> getSetupCommands()
	{
		Set<SlashCommandBuilder> builders = new HashSet<>();

		builders.add(new SlashCommandBuilder().setName("setupreact")
			.setDescription("Post the role message here")
			.addOption(SlashCommandOption.create(SlashCommandOptionType.ROLE, "role", "Role to give", true))
			.setDefaultEnabledForPermissions(PermissionType.ADMINISTRATOR)
			.setEnabledInDms(false)
		);
		builders.add(new SlashCommandBuilder().setName("setupstatus")
			.setDescription("Post the status message here")
			.setDefaultEnabledForPermissions(PermissionType.ADMINISTRATOR)
			.setEnabledInDms(false)
		);
		builders.add(new SlashCommandBuilder().setName("setupcontentqueue")
			.setDescription("Post the content queue message here")
			.setDefaultEnabledForPermissions(PermissionType.ADMINISTRATOR)
			.setEnabledInDms(false)
		);

		return builders;
	}
}
