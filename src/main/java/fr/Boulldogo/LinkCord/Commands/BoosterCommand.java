package fr.Boulldogo.LinkCord.Commands;

import java.time.Instant;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.Boulldogo.LinkCord.Main;
import fr.Boulldogo.LinkCord.Events.DiscordBoostRewardsEvent;
import fr.Boulldogo.LinkCord.Utils.YamlFileGestionnary;
import net.md_5.bungee.api.ChatColor;

public class BoosterCommand implements CommandExecutor {
	
	private final Main plugin;
	
	public BoosterCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	    if(!(sender instanceof Player)) {
	        sender.sendMessage("Only online players can use that command!");
	        return true;
	    }

	    String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";

	    Player player =(Player) sender;
	    YamlFileGestionnary ges = plugin.getYamlGestionnary();

	    if(!ges.playerExists(player.getUniqueId())) {
	        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-not-linked")));
	        return true;
	    }

	    if(!ges.playerIsBooster(player.getUniqueId())) {
	        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you-are-not-a-booster")));
	        return true;
	    } else {
	        if(!plugin.playerIsBooster(player)) {
	            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you-are-not-a-booster")));
	            return true;
	        }
	    }

	    if(!plugin.getConfig().getBoolean("enable-booster-rewards")) {
	        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.booster-rewards-disable")));
	        return true;
	    }

	    String cooldownPath = "boost-rewards-command-cooldown." + player.getName() + ".expire-timestamp";

	    long currentTimestamp = Instant.now().getEpochSecond();
	    long expireTimestamp = plugin.getConfig().getLong(cooldownPath, 0); 

	    if(expireTimestamp <= currentTimestamp) {
	        int timestampToAdd = plugin.getConfig().getInt("boost-rewards-command-cooldown");
	        long newExpireTimestamp = currentTimestamp + timestampToAdd;
	        plugin.getConfig().set(cooldownPath, newExpireTimestamp); 
	        plugin.saveConfig();
	        processBoosterRewardsGive(player);
	        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.rewards-correctly-given")));
	    } else {
	        long timeRemaining = expireTimestamp - currentTimestamp;
	        String formattedTime = plugin.formatTime(timeRemaining);
	        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.booster-rewards-cooldown").replace("%t", formattedTime)));
	    }

	    return true;
	}

	public void processBoosterRewardsGive(Player player) {
	    YamlFileGestionnary ges = plugin.getYamlGestionnary();
	    
	    String discordAccountName = ges.getDiscordTagByUUID(player.getUniqueId());
	    String discordAccountIDName = ges.getDiscordAccountIdByUUID(player.getUniqueId());
	    
		if(!plugin.getConfig().getStringList("executed-commands-for-booster-rewards").isEmpty()) {
			for(String command : plugin.getConfig().getStringList("executed-commands-for-booster-rewards")) {
				String finalCommand = command.replace("%player", player.getName());
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
				plugin.getLogger().info("Dispatch command /" + finalCommand + " for player " + player.getName() + "(Due to booster rewards)");
	            DiscordBoostRewardsEvent event = new DiscordBoostRewardsEvent(player.getName(),"/" + finalCommand, discordAccountName, discordAccountIDName);
	            Bukkit.getPluginManager().callEvent(event);
			}
		}
	}

    public String translateString(String s) {
    	return ChatColor.translateAlternateColorCodes('&', s);
    }
}
