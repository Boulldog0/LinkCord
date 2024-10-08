package fr.Boulldogo.LinkCord.Discord.Interface;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface SlashCommand {
    String getName();
    String getDescription();
    CommandData getCommandData();
    void execute(SlashCommandInteractionEvent event);
}
