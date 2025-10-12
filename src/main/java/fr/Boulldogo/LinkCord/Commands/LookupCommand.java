package fr.Boulldogo.LinkCord.Commands;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.Boulldogo.LinkCord.LinkCord;
import fr.Boulldogo.LinkCord.Utils.JSON.DiscordPlayerLink;
import fr.Boulldogo.LinkCord.Utils.JSON.LinksManager;
import net.md_5.bungee.api.ChatColor;

public class LookupCommand implements CommandExecutor {
	
	private final LinkCord plugin;
	
	private List<String> playerLookupList = new ArrayList<>();
	
	public LookupCommand(LinkCord plugin) {
		this.plugin = plugin;
		
		playerLookupList = plugin.getConfig().getStringList("lookup-message-using-nickname");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("Only online players can be use that command !");
			return true;
		}
		
    	String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
		
		Player player = (Player) sender;
		
		if(!player.hasPermission("linkcord.lookup")) {
			player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.no-permission")));
			return true;
		}
		
		if(args.length < 1) {
			player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.usage-lookup")));
			return true;
		}
		
		LinksManager ges = plugin.getLinksManager();
		
		String nicknameOrId = args[0];
		boolean isNickname = false;
		
		try {
			Integer.parseInt(nicknameOrId);
		} catch(NumberFormatException e) {
			isNickname = true;
		}
		
		
		DiscordPlayerLink link = null;
		
		if(isNickname) {
			@SuppressWarnings("deprecation")
			OfflinePlayer p = Bukkit.getOfflinePlayer(nicknameOrId);
			
			if(p == null) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.invalid-player")));
				return true;
			}
			
			UUID playerUUID = player.getUniqueId();
			
			if(!ges.isPlayerLinked(playerUUID)) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-not-linked")));
				return true;
			}
			
			link = ges.getLinkFor(playerUUID);
			
			if(link == null) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-not-linked")));
				return true;
			}
		} else {
			link = ges.composeForLookupWithDiscordId(nicknameOrId);
			
			if(link == null) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.no-account-linked-with-discord-id")));
				return true;
			}
			
			OfflinePlayer p = Bukkit.getOfflinePlayer(link.getPlayerUUID());
			
			if(p == null) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.invalid-player")));
				return true;
			}
		}
		
		String discordTag = link.getTag();
		String discordId = link.getDiscordId();
		boolean isBooster = link.isBoosting();
		String linkDate = getFormattedDateFor(link.getLinkTime());
		
		for(int i = 0; i < playerLookupList.size(); i++) {
			player.sendMessage(prefix + translateString(playerLookupList.get(i)
					.replace("%player", nicknameOrId)
					.replace("%discord_tag", discordTag)
					.replace("%discord_id", discordId)
					.replace("%link_date", linkDate)
					.replace("%is_booster", String.valueOf(isBooster))));
		}	
		return false;
	}
	
    private String getFormattedDateFor(Long ms) {
        LocalDateTime date = Instant.ofEpochMilli(ms)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return date.format(formatter);
    }
	
    public String translateString(String s) {
    	return ChatColor.translateAlternateColorCodes('&', s);
    }

}
