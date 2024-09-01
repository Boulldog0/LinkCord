package fr.Boulldogo.LinkCord.Discord.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.Boulldogo.LinkCord.Main;
import fr.Boulldogo.LinkCord.Discord.Interface.SlashCommand;
import fr.Boulldogo.LinkCord.Events.DiscordLinkEvent;
import fr.Boulldogo.LinkCord.Utils.LinkCodeUtils;
import fr.Boulldogo.LinkCord.Utils.PlayerUtils;
import fr.Boulldogo.LinkCord.Utils.YamlFileGestionnary;
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
import net.dv8tion.jda.api.Permission;

public class LinkCommand implements SlashCommand {

    private final Main plugin;

    public LinkCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "link";
    }

    @Override
    public String getDescription() {
        return "Give your in-game code for link your in-game account with your discord account !";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOptions(
                        new OptionData(OptionType.STRING, "code", "Given code in-game with command /link").setRequired(true)
                );
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        e.deferReply().queue();

        String code = e.getOption("code").getAsString();
        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";

        plugin.getLogger().info("Received link command with code: " + code);

        if(code == null) {
            sendErrorMessage(e, "The given code is null! Please retry to execute this command after.");
            return;
        }

        if(plugin.getConfig().getBoolean("restrict-commands-channel")) {
            if(e.getChannel().getIdLong() != plugin.getConfig().getLong("link-channel-id")) {
                MessageChannel channel = e.getGuild().getNewsChannelById(plugin.getConfig().getLong("link-channel-id"));
                sendErrorMessage(e, "You are not in the correct channel for linking your account! Channel: " + channel.getAsMention());
                return;
            }
        }

        int c;
        try {
            c = Integer.parseInt(code);
        } catch(NumberFormatException e2) {
            sendErrorMessage(e, "The given code is invalid! The code must be a valid integer!");
            return;
        }

        LinkCodeUtils codeUtils = plugin.getCodeUtils();

        if(!codeUtils.isCodeExists(c)) {
            sendErrorMessage(e, "This code does not exist! Please execute the command /link in-game first and stay connected.");
            return;
        }

        String playerName = codeUtils.getPlayerNameWithCode(c);
        Player player = Bukkit.getPlayer(playerName);

        if(player == null || !player.isOnline()) {
            sendErrorMessage(e, "The player linked with this code is not online or does not exist! Please execute the command /link in-game first and stay connected.");
            return;
        }

        UUID playerUUID = player.getUniqueId();
        YamlFileGestionnary gestionnary = plugin.getYamlGestionnary();

        if(gestionnary.playerExists(playerUUID)) {
            sendErrorMessage(e, "This player is already linked with an account! Please unlink the account first before relinking it.");
            return;
        }

        boolean isBoosting = e.getMember().isBoosting();
        @SuppressWarnings("deprecation")
		String accountName = e.getMember().getUser().getAsTag();
        String accountUUID = e.getMember().getUser().getId();

        if(gestionnary.discordAccountIdIsLinked(accountUUID)) {
            sendErrorMessage(e, "The Discord account is already linked with a Minecraft account!");
            return;
        }

        List<String> executedCommands = new ArrayList<>();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if(!plugin.getConfig().getStringList("executed-commands-when-player-first-link").isEmpty()) {
                if(!gestionnary.playerHasAlreadyLinked(playerUUID)) {
                    gestionnary.addLinkedPlayer(playerUUID);
                    for(String command : plugin.getConfig().getStringList("executed-commands-when-player-first-link")) {
                        String finalCommand = command.replace("%player", playerName);
                        plugin.getLogger().info("Dispatch command /" + finalCommand + " for player " + playerName + "(Due to first link)");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                        executedCommands.add("/" + finalCommand);
                    }
                }
            }

            if(!plugin.getConfig().getStringList("executed-commands-when-player-link").isEmpty()) {
                for(String command : plugin.getConfig().getStringList("executed-commands-when-player-link")) {
                    String finalCommand = command.replace("%player", playerName);
                    plugin.getLogger().info("Dispatch command /" + finalCommand + " for player " + playerName + "(Due to link)");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                    executedCommands.add("/" + finalCommand);
                }
            }
        });

        gestionnary.registerDataForPlayer(playerUUID, playerName, accountName, accountUUID, isBoosting);
        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-correctly-linked")
                .replace("%a", accountName)));

        if(plugin.getConfig().getBoolean("update-discord-user-username")) {
            tryUpdateNickname(e, player);
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(":white_check_mark: Success !");
        builder.setDescription(":paperclip: Your Discord account " + e.getMember().getAsMention() + " was correctly linked with the Minecraft account " + playerName + ".");
        builder.setAuthor("LinkCord", "https://www.spigotmc.org/resources/linkcord-1-7-1-21.119310/", "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");
        
        if(plugin.getConfig().getBoolean("remove-specific-roles-on-link")) {
        	for(String roleId : plugin.getConfig().getStringList("roles-to-remove-on-link")) {
	            Long finalId = Long.parseLong(roleId);
                plugin.checkAndDeleteDiscordRole(player, finalId);
        	}
        }

        MessageEmbed embed = builder.build();
        e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();

        Bukkit.getScheduler().runTask(plugin, () -> {
            DiscordLinkEvent event = new DiscordLinkEvent(playerName, executedCommands, accountName, accountUUID);
            Bukkit.getPluginManager().callEvent(event);
        });
        
        PlayerUtils pUtils = plugin.getPlayerUtils();
        pUtils.processPlayerVerifications(player);

        plugin.getLogger().info(playerName + " correctly linked his account with Discord account " + accountName + " !");
        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-correctly-linked")
                .replace("%a", accountName)));

        LinkCodeUtils utils = plugin.getCodeUtils();
        utils.removePlayer(player);
    }

    private void tryUpdateNickname(SlashCommandInteractionEvent e, Player player) {
        if(e.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
            if(e.getGuild().getSelfMember().canInteract(e.getMember())) {
                if(!player.getName().equals(e.getMember().getNickname())) {
                    e.getMember().modifyNickname(player.getName()).queue(
                        success -> plugin.getLogger().info("Nickname updated successfully for " + e.getMember().getEffectiveName()),
                        error -> plugin.getLogger().warning("Failed to update nickname for " + e.getMember().getEffectiveName() + ". Reason: " + error.getMessage())
                    );
                }
            } else {
                plugin.getLogger().warning("The bot cannot modify the member " + e.getMember().getEffectiveName() + " because their role is higher or equal to the bot's highest role.");
            }
        } else {
            plugin.getLogger().warning("The bot does not have permission to change nicknames on this server.");
        }
    }

    private void sendErrorMessage(SlashCommandInteractionEvent e, String message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Error !");
        builder.setDescription(":x: " + message);
        builder.setAuthor("LinkCord", "https://www.spigotmc.org/resources/linkcord-1-7-1-21.119310/", "https://i.ibb.co/wSprwQx/image-2024-08-30-014250385.jpg");

        MessageEmbed embed = builder.build();
        e.getHook().sendMessage(MessageCreateData.fromEmbeds(embed)).setEphemeral(true).queue();
    }

    public String translateString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
