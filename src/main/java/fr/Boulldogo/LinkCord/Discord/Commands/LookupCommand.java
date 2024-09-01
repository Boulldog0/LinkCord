package fr.Boulldogo.LinkCord.Discord.Commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import fr.Boulldogo.LinkCord.Main;
import fr.Boulldogo.LinkCord.Discord.Interface.SlashCommand;
import fr.Boulldogo.LinkCord.Utils.YamlFileGestionnary;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.md_5.bungee.api.ChatColor;

public class LookupCommand implements SlashCommand {

    private final Main plugin;

    public LookupCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "lookup";
    }

    @Override
    public String getDescription() {
        return "Allow to view what in-game account is link with a discord account or the opposite.";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOptions(
                		new OptionData(OptionType.BOOLEAN, "ephemeral", "Define if you want to show to everyone the result or no (True/False)").setRequired(true),
                        new OptionData(OptionType.MENTIONABLE, "user", "Give the discord member whose in-game nickname you want to obtain").setRequired(false),
                        new OptionData(OptionType.STRING, "pseudo", "Give the in-game nickname whose discord account you want to obtain").setRequired(false)
                );
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
    	e.deferReply().queue();
    	
    	Member user = e.getOption("user") != null ? e.getOption("user").getAsMember() : null;
    	String username = e.getOption("pseudo") != null ? e.getOption("pseudo").getAsString() : null;
    	boolean ephemeral = !e.getOption("ephemeral").getAsBoolean();
    	
    	Long requiredRole = plugin.getConfig().getLong("required-role-id-for-lookup-command");
    	
    	Role role = e.getGuild().getRoleById(requiredRole);
    	
    	if(role == null) {
    		EmbedBuilder builder = new EmbedBuilder();
    		builder.setTitle("Error !");
    		builder.setDescription(":x: The given role ID into the plugin configuration is wrong. Please report that to an administrator.");
    		builder.setAuthor("LinkCord", null, "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
    		
    		MessageEmbed embed = builder.build();
    		
    		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
    		return;
    	}
    	
    	if(!e.getMember().getRoles().contains(role)) {
    		EmbedBuilder builder = new EmbedBuilder();
    		builder.setTitle("Error !");
    		builder.setDescription(":x: You have not permission to execute this command. Required role : " + role.getAsMention());
    		builder.setAuthor("LinkCord", null, "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
    		
    		MessageEmbed embed = builder.build();
    		
    		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
    		return;
    	}
    	
    	if(user == null && username == null) {
    		EmbedBuilder builder = new EmbedBuilder();
    		builder.setTitle("Error !");
    		builder.setDescription(":x: Please give a discord user or a in-game nickname.");
    		builder.setAuthor("LinkCord", null, "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
    		
    		MessageEmbed embed = builder.build();
    		
    		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
    		return;
    	}
    	
    	if(user != null && username != null) {
    		EmbedBuilder builder = new EmbedBuilder();
    		builder.setTitle("Error !");
    		builder.setDescription(":x: Please give **ONLY** a discord user **OR** a in-game nickname.");
    		builder.setAuthor("LinkCord", null, "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
    		
    		MessageEmbed embed = builder.build();
    		
    		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
    		return;
    	}
    	
    	YamlFileGestionnary ges = plugin.getYamlGestionnary();
    	
    	if(user != null) {
    		Long accountId = user.getIdLong();
    		if(!ges.discordUUIDExists(accountId.toString())) {
        		EmbedBuilder builder = new EmbedBuilder();
        		builder.setTitle("Error !");
        		builder.setDescription(":x: Error : No linked account found with given user !");
        		builder.setAuthor("LinkCord", null, "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
        		
        		MessageEmbed embed = builder.build();
        		
        		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
        		return;
    		}
    		
    		String uname = ges.getPlayernameWithDiscordID(accountId.toString());
    		@SuppressWarnings("deprecation")
			UUID playerUUID = ges.getUUIDByDiscordTag(user.getUser().getAsTag().toString());
    		
    		EmbedBuilder builder = new EmbedBuilder();
    		builder.setTitle("Linked account found for " + user.getAsMention() + " !");
    		builder.addField("In-Game username :", uname, true);
    		builder.addField("In-Game UUID :", playerUUID.toString(), true);
    		builder.setAuthor("LinkCord", null, "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
    		
    		MessageEmbed embed = builder.build();
    		
    		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
    		return;
    	} else if(username != null) {
    		@SuppressWarnings("deprecation")
			OfflinePlayer player = Bukkit.getOfflinePlayer(username);
    		
    		if(player == null) {
        		EmbedBuilder builder = new EmbedBuilder();
        		builder.setTitle("Error !");
        		builder.setDescription(":x: Error : No player found with the given nickname !");
        		builder.setAuthor("LinkCord", null, "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
        		
        		MessageEmbed embed = builder.build();
        		
        		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
        		return;
    		}
    		
    		UUID playerUUID = player.getUniqueId();
    		
    		if(!ges.playerExists(playerUUID)) {
        		EmbedBuilder builder = new EmbedBuilder();
        		builder.setTitle("Error !");
        		builder.setDescription(":x: Error : The given player doesn't have a linked account!");
        		builder.setAuthor("LinkCord", null, "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
        		
        		MessageEmbed embed = builder.build();
        		
        		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
        		return;
    		}
    		
    		Long discordId = Long.valueOf(ges.getDiscordAccountIdByUUID(playerUUID));
    		String tag = ges.getDiscordTagByUUID(playerUUID);
    		Member member = e.getGuild().getMemberById(discordId);
    		String linkDate = ges.getLinkDateForUUID(playerUUID);
    		
    		if(member == null) {	
        		EmbedBuilder builder = new EmbedBuilder();
        		builder.setTitle("Linked account found for " + username + " !");
        		builder.setDescription(":warning: The linked discord account is not found in this discord server ! :warning:");
        		builder.addField("Discord ID :", discordId.toString(), true);
        		builder.addField("Account linked at :", linkDate, true);
        		builder.addField("Last Registred Discord Tag :", tag, true);
        		builder.setAuthor("LinkCord", null, "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
        		
        		MessageEmbed embed = builder.build();
        		
        		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(ephemeral).queue();
        		return;
    		} else {
        		EmbedBuilder builder = new EmbedBuilder();
        		builder.setTitle("Linked account found for " + username + " !");
        		builder.addField("Discord Tag :", member.getAsMention(), true);
        		builder.addField("Discord ID :", discordId.toString(), true);
        		builder.addField("Account linked at :", linkDate, true);
        		builder.addField("Last Registred Discord Tag :", tag, true);
        		builder.setAuthor("LinkCord", null, "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
        		
        		MessageEmbed embed = builder.build();
        		
        		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(ephemeral).queue();
        		return;
    		}
    	}
    }
    
    public String translateString(String s) {
    	return ChatColor.translateAlternateColorCodes('&', s);
    }
}
