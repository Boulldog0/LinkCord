package fr.Boulldogo.LinkCord.Discord.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.Boulldogo.LinkCord.LinkCord;
import fr.Boulldogo.LinkCord.Discord.Interface.SlashCommand;
import fr.Boulldogo.LinkCord.Events.DiscordRewardsCommandEvent;
import fr.Boulldogo.LinkCord.Events.DiscordUnlinkEvent;
import fr.Boulldogo.LinkCord.Events.RewardReason;
import fr.Boulldogo.LinkCord.Utils.LinkCodeUtils;
import fr.Boulldogo.LinkCord.Utils.PlayerUtils;
import fr.Boulldogo.LinkCord.Utils.JSON.LinksManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.md_5.bungee.api.ChatColor;

public class UnlinkCommand implements SlashCommand {

    private final LinkCord plugin;
    
    private List<String> unlinkCommands = new ArrayList<>();
    
    private boolean selfUnlinkDisable;
    private Long restrictChannelId = 0L;

    public UnlinkCommand(LinkCord plugin) {
        this.plugin = plugin;
        
        unlinkCommands = plugin.getConfig().getStringList("executed-commands-when-player-unlink");
        
        selfUnlinkDisable = plugin.getConfig().getBoolean("disable-self-unlink");
        if(plugin.getConfig().getBoolean("restrict-commands-channel")) {
        	restrictChannelId = plugin.getConfig().getLong("link-channel-id");
        }
    }

    @Override
    public String getName() {
        return "unlink";
    }

    @Override
    public String getDescription() {
        return "Give your in-game code for unlink your in-game account from your discord account !";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOptions(
                        new OptionData(OptionType.STRING, "code", "Given code in-game with command /unlink").setRequired(true)
                );
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        e.deferReply().queue(
                success -> {            	
                	String code = e.getOption("code").getAsString();
                	String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
                	
                	if(code == null) {
                		EmbedBuilder builder = new EmbedBuilder();
                		builder.setTitle("Error !");
                		builder.setDescription(":x: The given code is null ! Please retry to execute this command after.");
                		builder.setAuthor("LinkCord", "https://www.spigotmc.org/resources/linkcord-1-7-1-21.119310/", "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
                		
                		MessageEmbed embed = builder.build();
                		
                		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
                	}
                	
                    if(restrictChannelId != 0L && e.getChannel().getIdLong() != restrictChannelId) {
                		MessageChannel channel = e.getGuild().getNewsChannelById(plugin.getConfig().getInt("link-channel-id"));
                		EmbedBuilder builder = new EmbedBuilder();
                		builder.setTitle("Error !");
                		builder.setDescription(":x: You are not in the good channel for unlink your account ! Channel : " + channel.getAsMention());
                		builder.setAuthor("LinkCord", "https://www.spigotmc.org/resources/linkcord-1-7-1-21.119310/", "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
                		
                		MessageEmbed embed = builder.build();
                		
                		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
                		return;
                    }
                	
            		if(selfUnlinkDisable) {
                		EmbedBuilder builder = new EmbedBuilder();
                		builder.setTitle("Error !");
                		builder.setDescription(":x: The self unlink is disabled in this server ! Please contact the staff for more informations.");
                		builder.setAuthor("LinkCord", "https://www.spigotmc.org/resources/linkcord-1-7-1-21.119310/", "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
                		
                		MessageEmbed embed = builder.build();
                		
                		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
                		return;
            		}
                	
                	int c = 0;
                	
                	try {
                		c = Integer.parseInt(code);
                	} catch(NumberFormatException e2) {
                		EmbedBuilder builder = new EmbedBuilder();
                		builder.setTitle("Error !");
                		builder.setDescription(":x: The given code is invalid ! The code must be a valid integer !");
                		    		builder.setAuthor("LinkCord", "https://www.spigotmc.org/resources/linkcord-1-7-1-21.119310/", "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
                		
                		MessageEmbed embed = builder.build();
                		
                		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
                		return;
                	}
                	
                	LinkCodeUtils codeUtils = plugin.getCodeUtils();
                	
                	if(!codeUtils.isCodeExists(c)) {
                		EmbedBuilder builder = new EmbedBuilder();
                		builder.setTitle("Error !");
                		builder.setDescription(":x: This code does not exists ! Please execute firstly the command /unlink in-game and stay connected.");
                		builder.setAuthor("LinkCord", "https://www.spigotmc.org/resources/linkcord-1-7-1-21.119310/", "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
                		
                		MessageEmbed embed = builder.build();
                		
                		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
                		return;
                	}
                	
                	String playerName = codeUtils.getPlayerNameWithCode(c);
                	
                	Player player = null;
                	
                	if(playerName != null) {
                    	player = Bukkit.getPlayer(playerName);
                    	if(player == null || !player.isOnline()) {
                    		EmbedBuilder builder = new EmbedBuilder();
                    		builder.setTitle("Error !");
                    		builder.setDescription(":x: The player linked with this code is not online or not exists ! Please execute firstly the command /unlink in-game and stay connected.");
                    		builder.setAuthor("LinkCord", "https://www.spigotmc.org/resources/linkcord-1-7-1-21.119310/", "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
                    		
                    		MessageEmbed embed = builder.build();
                    		
                    		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
                    		return;
                    	}	
                	} else {
                		EmbedBuilder builder = new EmbedBuilder();
                		builder.setTitle("Error !");
                		builder.setDescription(":x: The player linked with this code is not online or not exists ! Please execute firstly the command /unlink in-game and stay connected.");
                		builder.setAuthor("LinkCord", "https://www.spigotmc.org/resources/linkcord-1-7-1-21.119310/", "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
                		
                		MessageEmbed embed = builder.build();
                		
                		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
                		return;
                	}
                	
                	UUID playerUUID = player.getUniqueId();
                	
                	LinksManager ges = plugin.getLinksManager();
                	if(!ges.isPlayerLinked(playerUUID)) {
                		EmbedBuilder builder = new EmbedBuilder();
                		builder.setTitle("Error !");
                		builder.setDescription(":x: This player is not linked with an account !");
                		builder.setAuthor("LinkCord", "https://www.spigotmc.org/resources/linkcord-1-7-1-21.119310/", "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
                		
                		MessageEmbed embed = builder.build();
                		
                		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
                		return;
                	}    	
                	
            		@SuppressWarnings("deprecation")
            		String accountName = e.getMember().getUser().getAsTag();
                	String accountID = e.getMember().getUser().getId();  	
            		
            		PlayerUtils u = plugin.getPlayerUtils();
            		u.removeAllLinkedRoles(player);
            		
            		List<String> executedCommands = new ArrayList<>();
            		
                    Bukkit.getScheduler().runTask(plugin, () -> {
                    	if(!unlinkCommands.isEmpty()) {
                    		unlinkCommands.forEach(command -> {
                				String finalCommand = command.replace("%player", playerName);
                				plugin.getLogger().info("Dispatch command /" + finalCommand + " for player " + playerName + " (Due to unlink)");
                				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                				executedCommands.add("/" + finalCommand);
        	    				DiscordRewardsCommandEvent event = new DiscordRewardsCommandEvent(playerName, "/" + finalCommand, accountName, accountID, RewardReason.UNLINK);
        	    				Bukkit.getServer().getPluginManager().callEvent(event);
                    		});
                    	}
                    });    

            		EmbedBuilder builder = new EmbedBuilder();
            		builder.setTitle(":white_check_mark: Success !");
            		builder.setDescription(":paperclip: Your discord account " + e.getMember().getAsMention() + " was correctly unlinked from the minecraft account " + playerName + ".");
            		builder.setAuthor("LinkCord", "https://www.spigotmc.org/resources/linkcord-1-7-1-21.119310/", "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
            		
            		MessageEmbed embed = builder.build();
            		
            		Bukkit.getScheduler().runTask(plugin, () -> {
            			DiscordUnlinkEvent event = new DiscordUnlinkEvent(playerName, executedCommands, accountName, accountID);
            			Bukkit.getPluginManager().callEvent(event);	
            		});
                	
                	player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-correctly-unlinked")));	
                	
                	plugin.getLogger().info(playerName + " correctly unlinked his account from discord account " + accountName + " !"); 
                	
                	LinkCodeUtils utils = plugin.getCodeUtils();
                	utils.removePlayer(player);
                	
                	ges.removePlayer(playerUUID);
                	
            		e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
                },
                failure -> {
                    plugin.getLogger().warning("Failed to defer reply: " + failure.getMessage());
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle(":x: Error !");
                    builder.setDescription("An error occured when trying to unlink your account. Please retry after or contact the staff for more informations.");
                    builder.setAuthor("LinkCord", "https://www.spigotmc.org/resources/linkcord-1-7-1-21.119310/", "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
                    MessageEmbed embed = builder.build();
                    e.reply(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
                }
            );
    }
    
    public String translateString(String s) {
    	return ChatColor.translateAlternateColorCodes('&', s);
    }
}

