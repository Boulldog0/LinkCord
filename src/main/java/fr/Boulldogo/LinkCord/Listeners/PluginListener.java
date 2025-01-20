package fr.Boulldogo.LinkCord.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.Boulldogo.LinkCord.Main;
import fr.Boulldogo.LinkCord.Events.DiscordBoosterStatusChangeEvent;
import fr.Boulldogo.LinkCord.Events.DiscordLinkEvent;
import fr.Boulldogo.LinkCord.Events.DiscordRewardsCommandEvent;
import fr.Boulldogo.LinkCord.Events.DiscordRoleAddEvent;
import fr.Boulldogo.LinkCord.Events.DiscordRoleRemoveEvent;
import fr.Boulldogo.LinkCord.Events.DiscordUnlinkEvent;
import fr.Boulldogo.LinkCord.Utils.YamlFileGestionnary;

public class PluginListener implements Listener {
	
	private final Main plugin;
	
	public PluginListener(Main plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		plugin.getPlayerUtils().processPlayerVerifications(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		plugin.getPlayerUtils().processPlayerVerifications(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerBoosterStatusChange(DiscordBoosterStatusChangeEvent e) {
	    YamlFileGestionnary ges = plugin.getYamlGestionnary();
	    
	    String discordAccountName = ges.getDiscordTagByUUID(e.getPlayer().getUniqueId());
	    String discordAccountIDName = ges.getDiscordAccountIdByUUID(e.getPlayer().getUniqueId());
		if(e.isBoosting()) {
			if(plugin.getConfig().getStringList("executed-commands-when-boost").isEmpty()) return;
			if(!plugin.getConfig().getBoolean("execute-commands-when-boost")) return;
			for(String s : plugin.getConfig().getStringList("executed-commands-when-boost")) {
				String finalCommand = s.replace("%player", e.getPlayer().getName());
				plugin.getLogger().info("Dispatch command /" + finalCommand + " for player " + e.getPlayer().getName() + "(Due to booster status change)");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
				DiscordRewardsCommandEvent event = new DiscordRewardsCommandEvent(e.getPlayer().getName(), "/" + finalCommand, discordAccountName, discordAccountIDName);
				Bukkit.getServer().getPluginManager().callEvent(event);
			}
		} else {
			if(plugin.getConfig().getStringList("executed-commands-when-unboost").isEmpty()) return;
			if(!plugin.getConfig().getBoolean("execute-commands-when-unboost")) return;
			for(String s : plugin.getConfig().getStringList("executed-commands-when-unboost")) {
				String finalCommand = s.replace("%player", e.getPlayer().getName());
				plugin.getLogger().info("Dispatch command /" + finalCommand + " for player " + e.getPlayer().getName() + "(Due to booster status change)");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
				DiscordRewardsCommandEvent event = new DiscordRewardsCommandEvent(e.getPlayer().getName(), "/" + finalCommand, discordAccountName, discordAccountIDName);
				Bukkit.getServer().getPluginManager().callEvent(event);
			}
		}
	}
	
	@EventHandler
	public void rewardsCommandExecute(DiscordRewardsCommandEvent e) {
		if(Bukkit.getPluginManager().isPluginEnabled("WatchLogs")) {
			Player p = Bukkit.getPlayer(e.getPlayerName());
			plugin.getWatchLogsAPI().addCustomLog("linkcord-rewards-command", p, plugin.getWatchLogsAPI().getFormattedLocationString(p.getLocation()), p.getWorld().getName(), "Executed command : " + e.getExecutedCommands());
		}
	}
	
	@EventHandler
	public void onDiscordLink(DiscordLinkEvent e) {
		if(Bukkit.getPluginManager().isPluginEnabled("WatchLogs")) {
			Player p = Bukkit.getPlayer(e.getPlayerName());
			plugin.getWatchLogsAPI().addCustomLog("discord-link", p, plugin.getWatchLogsAPI().getFormattedLocationString(p.getLocation()), p.getWorld().getName(), "Discord account name : " + e.getDiscordAccount() + " | Discord account ID : " + e.getDiscordAccountId());
		}
	}
	
	@EventHandler
	public void onDiscordUnlink(DiscordUnlinkEvent e) {
		if(Bukkit.getPluginManager().isPluginEnabled("WatchLogs")) {
			Player p = Bukkit.getPlayer(e.getPlayerName());
			plugin.getWatchLogsAPI().addCustomLog("discord-unlink", p, plugin.getWatchLogsAPI().getFormattedLocationString(p.getLocation()), p.getWorld().getName(), "Discord account name : " + e.getDiscordAccount() + " | Discord account ID : " + e.getDiscordAccountId());
		}
	}
	
	@EventHandler
	public void onDiscordAddRole(DiscordRoleAddEvent e) {
		if(Bukkit.getPluginManager().isPluginEnabled("WatchLogs")) {
			Player p = Bukkit.getPlayer(e.getPlayerName());
			plugin.getWatchLogsAPI().addCustomLog("discord-role-add", p, plugin.getWatchLogsAPI().getFormattedLocationString(p.getLocation()), p.getWorld().getName(), "Discord account name : " + e.getDiscordAccount() + " | Discord account ID : " + e.getDiscordAccountId() + " | Role name : " + e.getRoleName());
		}
	}
	
	@EventHandler
	public void onDiscordRemoveRole(DiscordRoleRemoveEvent e) {
		if(Bukkit.getPluginManager().isPluginEnabled("WatchLogs")) {
			Player p = Bukkit.getPlayer(e.getPlayerName());
			plugin.getWatchLogsAPI().addCustomLog("discord-role-remove", p, plugin.getWatchLogsAPI().getFormattedLocationString(p.getLocation()), p.getWorld().getName(), "Discord account name : " + e.getDiscordAccount() + " | Discord account ID : " + e.getDiscordAccountId() + " | Role name : " + e.getRoleName());
		}
	}

}
