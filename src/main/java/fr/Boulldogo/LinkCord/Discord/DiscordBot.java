package fr.Boulldogo.LinkCord.Discord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.security.auth.login.LoginException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.Boulldogo.LinkCord.LinkCord;
import fr.Boulldogo.LinkCord.Discord.Commands.LinkCommand;
import fr.Boulldogo.LinkCord.Discord.Commands.LookupCommand;
import fr.Boulldogo.LinkCord.Discord.Commands.UnlinkCommand;
import fr.Boulldogo.LinkCord.Discord.Interface.SlashCommand;
import fr.Boulldogo.LinkCord.Events.DiscordRoleAddEvent;
import fr.Boulldogo.LinkCord.Events.DiscordRoleRemoveEvent;
import fr.Boulldogo.LinkCord.Utils.JSON.DiscordPlayerLink;
import fr.Boulldogo.LinkCord.Utils.JSON.LinksManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class DiscordBot extends ListenerAdapter {

    private final Map<String, SlashCommand> commands = new HashMap<>();
    private JDA jda;
    private final LinkCord plugin;
    
    private LinksManager ges;
    private BotExtension extension; 
    private long guildId;
    private boolean updateNickname;

    public DiscordBot(LinkCord plugin) {
        this.plugin = plugin;
        
        ges = plugin.getLinksManager();
        guildId = Long.parseLong(plugin.getConfig().getString("discord.guild-id"));
        updateNickname = plugin.getConfig().getBoolean("update-discord-user-username");
    }

    public void startBot() throws LoginException {
        String token = plugin.getConfig().getString("discord.bot-token");

        if(token == null || token.isEmpty()) {
            throw new LoginException("[Discord-Module] Discord token is missing in configuration. Discord Module is disabled.");
        }

        JDABuilder jdaBuilder = JDABuilder.create(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MEMBERS);
        jdaBuilder.setActivity(Activity.playing(plugin.getConfig().contains("discord.activity_message") ? plugin.getConfig().getString("discord.activity_message") : "🖇️ Link a lot of players"));
        jdaBuilder.addEventListeners(this);
        jdaBuilder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        jdaBuilder.setAutoReconnect(true);

        jda = jdaBuilder.build();

        registerCommands();
        updateCommands();
        
        this.extension = new BotExtension(plugin, jda, plugin.getLinksManager());
    }
    
    public BotExtension getBotExtension() {
    	return extension;
    }
    
    private void registerCommands() {
    	addCommand(new LinkCommand(plugin));
    	addCommand(new UnlinkCommand(plugin));
    	addCommand(new LookupCommand(plugin));
    }
    
    public void addEventListener(ListenerAdapter adapter) {
    	jda.addEventListener(adapter);
    }
    
    public void addCommand(SlashCommand command) {
        commands.put(command.getName(), command);
    }

    private void updateCommands() {
        List<CommandData> commandData = new ArrayList<>();
        for(SlashCommand command : commands.values()) {
            commandData.add(command.getCommandData());
        }
        jda.updateCommands()
            .addCommands(commandData)
            .queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        SlashCommand command = commands.get(event.getName());
        if(command != null) {
            command.execute(event);
        }
    }

    public JDA getJDA() {
        return jda;
    }
    
    @SuppressWarnings("deprecation")
	public void processPlayerVerifications(Player player) {
		UUID playerUUID = player.getUniqueId();
    	if(ges.isPlayerLinked(playerUUID)) {
    		DiscordPlayerLink link = ges.getLinkFor(playerUUID);
    		
    		String accountName = link.getTag();
    		Guild guild = jda.getGuildById(guildId);
    		Member discordUser = guild.getMemberById(link.getDiscordId());
    		
    		if(guild.getMember(discordUser) != null) {
    			if(!discordUser.getUser().getAsTag().equals(accountName)) {
    			    link.setDiscordTag(discordUser.getUser().getAsTag());  
    			}
    			
    			if(!link.isBoosting()) {
    				if(discordUser.isBoosting()) {
    					link.setBoosting(true);
    				}
    			} else {
    				if(!discordUser.isBoosting()) {
    					link.setBoosting(false);
    				}
    			}
    		}
    		
    		if(updateNickname) {
    			if(discordUser.getNickname() != player.getName()) {
    				discordUser.modifyNickname(player.getName());
    			}
    		}
    	}
    }
    
    @SuppressWarnings("deprecation")
    public void checkAndAddRolesForPlayer(OfflinePlayer player, String roleId) {
        UUID playerUUID = player.getUniqueId();
        if(ges.isPlayerLinked(playerUUID)) {
    		DiscordPlayerLink link = ges.getLinkFor(playerUUID);
            Guild guild = jda.getGuildById(guildId);
            if(guild == null) {
                plugin.getLogger().info("Error: Guild with ID " + plugin.getConfig().getString("discord.guild-id") + " does not exist!");
                return;
            }

            String discordUserId = link.getDiscordId();
            if(discordUserId == null) {
                plugin.getLogger().info("Error: No Discord user linked for UUID " + playerUUID);
                return;
            }

            Member discordUser = guild.retrieveMemberById(discordUserId).complete();
            if(discordUser == null) {
                plugin.getLogger().info("Error: Member with ID " + discordUserId + " does not exist in the guild!");
                return;
            }

            Role roleToVerify = guild.getRoleById(roleId);
            if(roleToVerify == null) {
                plugin.getLogger().info("Error: Role with ID " + roleId + " does not exist in the discord server!");
                return;
            }

            Member botMember = guild.getSelfMember();
            int botRolePosition = botMember.getRoles().isEmpty() ? -1 : botMember.getRoles().get(0).getPosition();

            if(roleToVerify.getPosition() >= botRolePosition) {
                plugin.getLogger().warning("Error: Cannot add role " + roleToVerify.getName() + " to " + discordUser.getUser().getAsTag() + " because it is higher or equal in position than the bot's highest role.");
                return;
            }

            if(!discordUser.getRoles().contains(roleToVerify)) {
                guild.addRoleToMember(discordUser, roleToVerify).queue(
                    success -> {
                        plugin.getLogger().info("Successfully added role " + roleToVerify.getName() + " to discord user " + discordUser.getUser().getAsTag());
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            DiscordRoleAddEvent event = new DiscordRoleAddEvent(player.getName(), roleToVerify.getName(), link.getTag(), link.getDiscordId());
                            Bukkit.getPluginManager().callEvent(event);
                        });
                    },
                    error -> plugin.getLogger().warning("Failed to add role: " + error.getMessage())
                );
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void checkAndRemoveRolesForPlayer(OfflinePlayer player, String roleId) {
        UUID playerUUID = player.getUniqueId();
        if(ges.isPlayerLinked(playerUUID)) {
    		DiscordPlayerLink link = ges.getLinkFor(playerUUID);
            Guild guild = jda.getGuildById(guildId);
            if(guild == null) {
                plugin.getLogger().info("Error: Guild with ID " + plugin.getConfig().getString("discord.guild-id") + " does not exist!");
                return;
            }

            String discordUserId = link.getDiscordId();
            if(discordUserId == null) {
                plugin.getLogger().info("Error: No Discord user linked for UUID " + playerUUID);
                return;
            }

            Member discordUser = guild.retrieveMemberById(discordUserId).complete();
            if(discordUser == null) {
                plugin.getLogger().info("Error: Member with ID " + discordUserId + " does not exist in the guild!");
                return;
            }

            Role roleToVerify = guild.getRoleById(roleId);
            if(roleToVerify == null) {
                plugin.getLogger().info("Error: Role with ID " + roleId + " does not exist in the discord server!");
                return;
            }

            Member botMember = guild.getSelfMember();
            int botRolePosition = botMember.getRoles().isEmpty() ? -1 : botMember.getRoles().get(0).getPosition();

            if(roleToVerify.getPosition() >= botRolePosition) {
                plugin.getLogger().warning("Error: Cannot remove role " + roleToVerify.getName() + " from " + discordUser.getUser().getAsTag() + " because it is higher or equal in position than the bot's highest role.");
                return;
            }

            if(discordUser.getRoles().contains(roleToVerify)) {
                guild.removeRoleFromMember(discordUser, roleToVerify).queue(
                    success -> {
                        plugin.getLogger().info("Successfully removed role " + roleToVerify.getName() + " from discord user " + discordUser.getUser().getAsTag());
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            DiscordRoleRemoveEvent event = new DiscordRoleRemoveEvent(player.getName(), roleToVerify.getName(), link.getTag(), link.getDiscordId());
                            Bukkit.getPluginManager().callEvent(event);
                        });
                    },
                    error -> plugin.getLogger().warning("Failed to remove role: " + error.getMessage())
                );
            }
        }
    }
  
    public boolean playerIsBooster(Player player) {     
        UUID playerUUID = player.getUniqueId();
        if(ges.isPlayerLinked(playerUUID)) {
    		DiscordPlayerLink link = ges.getLinkFor(playerUUID);
            Guild guild = jda.getGuildById(guildId);
            if(guild == null) {
                plugin.getLogger().info("Error: Guild with ID " + plugin.getConfig().getString("discord.guild-id") + " does not exist!");
                return false;
            }

            String discordUserId = link.getDiscordId();
            if(discordUserId == null) {
                plugin.getLogger().info("Error: No Discord user linked for UUID " + playerUUID);
                return false;
            }

            Member discordUser = guild.retrieveMemberById(discordUserId).complete();
            
            if(discordUser == null) {
                plugin.getLogger().severe("Member with ID " + discordUserId + " not found in guild " + guild.getName());
                return false;
            }
            return discordUser.isBoosting();
        }
        return false;
    }

}
