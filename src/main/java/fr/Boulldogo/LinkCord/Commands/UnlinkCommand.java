package fr.Boulldogo.LinkCord.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.Boulldogo.LinkCord.Main;
import fr.Boulldogo.LinkCord.Events.DiscordUnlinkEvent;
import fr.Boulldogo.LinkCord.Utils.LinkCodeUtils;
import fr.Boulldogo.LinkCord.Utils.PlayerUtils;
import fr.Boulldogo.LinkCord.Utils.YamlFileGestionnary;
import net.md_5.bungee.api.ChatColor;

public class UnlinkCommand implements CommandExecutor {
	
	private final Main plugin;
	
	public UnlinkCommand(Main plugin) {
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
		
		if(!player.hasPermission("linkcord.unlink")) {
			player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.no-permission")));
			return true;
		}
		
		if(args.length < 1) {
			YamlFileGestionnary ges = plugin.getYamlGestionnary();
			
			if(plugin.getConfig().getBoolean("disable-self-unlink")) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.self-unlink-disable")));
				return true;
			}
			
			if(!ges.playerExists(player.getUniqueId())) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-not-linked")));
				return true;
			}
			
			LinkCodeUtils utils = plugin.getCodeUtils();
			int code = utils.getPlayerCode(player);
			
			player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.unlink-give-code")
					.replace("%code", String.valueOf(code))));
			return true;
		} else {
			if(!player.hasPermission("linkcord.force-unlink")) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.no-permission")));
				return true;
			}
			
			if(args.length < 2) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.usage-unlink-force")));
				return true;
			}
			
			if(!args[0].equals("force")) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.usage-unlink-force")));
				return true;
			}
			
			String playerName = args[1];
			
			@SuppressWarnings("deprecation")
			OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
			
			if(p == null) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.invalid-player")));
				return true;
			}
			
			UUID playerUUID = p.getUniqueId();
			YamlFileGestionnary ges = plugin.getYamlGestionnary();
			
			if(!ges.playerExists(playerUUID)) {
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-not-linked")));
				return true;
			}
			
			PlayerUtils utils = plugin.getPlayerUtils();
			utils.removeAllLinkedRoles(p);
			
			String accountName = ges.getDiscordTagByUUID(playerUUID);
	    	String accountUUID = ges.getDiscordAccountIdByUUID(playerUUID);
			
			List<String> executedCommands = new ArrayList<>();
			
	        Bukkit.getScheduler().runTask(plugin, () -> {
	    		if(!plugin.getConfig().getStringList("executed-commands-when-player-unlink").isEmpty()) {
	    			for(String cmd : plugin.getConfig().getStringList("executed-commands-when-player-unlink")) {
	    				String finalCommand = cmd.replace("%player", playerName);
	    				plugin.getLogger().info("Dispatch command /" + finalCommand + " for player " + playerName + " (Due to force unlink)");
	    				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
	    				executedCommands.add("/" + finalCommand);
	    			}
	    		}     	
	        });
			
			Bukkit.getScheduler().runTask(plugin, () -> {
				DiscordUnlinkEvent event = new DiscordUnlinkEvent(playerName, executedCommands, accountName, accountUUID);
				Bukkit.getPluginManager().callEvent(event);	
			});
			
			if(plugin.getConfig().getBoolean("remove-player-linked-roles-on-unlink")) {
				plugin.getPlayerUtils().removeAllLinkedRoles(player);
			}	
	    	
	    	ges.removePlayerFromFile(playerUUID);
	    	
	    	if(p.isOnline()) {
		    	player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-correctly-unlinked")));
	    	}
	    	
	    	plugin.getLogger().info(playerName + " correctly unlinked his account from discord account " + accountName + " !");  	

			player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-correctly-unlinked")));
			return true;	
		}
	}

    public String translateString(String s) {
    	return ChatColor.translateAlternateColorCodes('&', s);
    }
}
