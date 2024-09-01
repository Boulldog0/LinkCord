package fr.Boulldogo.LinkCord.Commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.Boulldogo.LinkCord.Main;
import fr.Boulldogo.LinkCord.Utils.YamlFileGestionnary;
import net.md_5.bungee.api.ChatColor;

public class LookupCommand implements CommandExecutor {
	
	private final Main plugin;
	
	public LookupCommand(Main plugin) {
		this.plugin = plugin;
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
		
		YamlFileGestionnary ges = plugin.getYamlGestionnary();
		
		String nicknameOrId = args[0];
		boolean isNickname = false;
		
		try {
			Integer.parseInt(nicknameOrId);
		} catch(NumberFormatException e) {
			isNickname = true;
		}
		
		if(isNickname) {
			@SuppressWarnings("deprecation")
			OfflinePlayer p = Bukkit.getOfflinePlayer(nicknameOrId);
			
			if(p == null) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.invalid-player")));
				return true;
			}
			
			UUID playerUUID = player.getUniqueId();
			
			if(!ges.playerExists(playerUUID)) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-not-linked")));
				return true;
			}
			
			String discordTag = ges.getDiscordTagByUUID(playerUUID);
			String discordId = ges.getDiscordAccountIdByUUID(playerUUID);
			boolean isBooster = ges.playerIsBooster(playerUUID);
			String linkDate = ges.getLinkDateForUUID(playerUUID);
			
			List<String> playerLookupList = plugin.getConfig().getStringList("lookup-message-using-nickname");
			
			for(int i = 0; i < playerLookupList.size(); i++) {
				player.sendMessage(prefix + translateString(playerLookupList.get(i)
						.replace("%player", nicknameOrId)
						.replace("%discord_tag", discordTag)
						.replace("%discord_id", discordId)
						.replace("%link_date", linkDate)
						.replace("%is_booster", String.valueOf(isBooster))));
			}
		} else {
			String player_name = ges.getPlayernameWithDiscordID(nicknameOrId);
			
			if(player_name == null) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.no-account-linked-with-discord-id")));
				return true;
			}
			
			@SuppressWarnings("deprecation")
			OfflinePlayer p = Bukkit.getOfflinePlayer(player_name);
			UUID playerUUID = player.getUniqueId();
			
			if(p == null) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.invalid-player")));
				return true;
			}
			
			String discordTag = ges.getDiscordTagByUUID(playerUUID);
			boolean isBooster = ges.playerIsBooster(playerUUID);
			String linkDate = ges.getLinkDateForUUID(playerUUID);
			
			List<String> playerLookupList = plugin.getConfig().getStringList("lookup-message-using-discord-id");
			
			for(int i = 0; i < playerLookupList.size(); i++) {
				player.sendMessage(prefix + translateString(playerLookupList.get(i)
						.replace("%player", player_name)
						.replace("%discord_tag", discordTag)
						.replace("%discord_id", nicknameOrId)
						.replace("%link_date", linkDate)
						.replace("%is_booster", String.valueOf(isBooster))));
			}
		}
		
		return false;
	}
	
    public String translateString(String s) {
    	return ChatColor.translateAlternateColorCodes('&', s);
    }

}
