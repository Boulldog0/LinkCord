package fr.Boulldogo.LinkCord.Listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.Boulldogo.LinkCord.LinkCord;
import fr.Boulldogo.LinkCord.Events.DiscordBoosterStatusChangeEvent;
import fr.Boulldogo.LinkCord.Events.DiscordLinkEvent;
import fr.Boulldogo.LinkCord.Events.DiscordRewardsCommandEvent;
import fr.Boulldogo.LinkCord.Events.DiscordRoleAddEvent;
import fr.Boulldogo.LinkCord.Events.DiscordRoleRemoveEvent;
import fr.Boulldogo.LinkCord.Events.DiscordUnlinkEvent;
import fr.Boulldogo.LinkCord.Events.RewardReason;
import fr.Boulldogo.LinkCord.Utils.JSON.DiscordPlayerLink;
import fr.Boulldogo.LinkCord.Utils.JSON.LinksManager;

public class PluginListener implements Listener {
	
	private final LinkCord plugin;
	private boolean isWatchLogsEnable;
	private LinksManager ges;
	private boolean removeDatas;
	
	private List<String> boostExecutedCommands = new ArrayList<>();
	private List<String> unboostExecutedCommands = new ArrayList<>();
	
	public PluginListener(LinkCord plugin) {
		this.plugin = plugin;
		this.isWatchLogsEnable = Bukkit.getPluginManager().isPluginEnabled("WatchLogs");
		this.ges = plugin.getLinksManager();
		
		this.removeDatas = plugin.getConfig().getBoolean("empty-player-datas-in-ram-on-disconnect");		
		if(plugin.getConfig().getBoolean("execute-commands-when-boost")) {
			this.boostExecutedCommands = plugin.getConfig().getStringList("executed-commands-when-boost");
		}
		if(plugin.getConfig().getBoolean("execute-commands-when-unboost")) {
			this.unboostExecutedCommands = plugin.getConfig().getStringList("executed-commands-when-unboost");
		}
	}
	
	@EventHandler
	public void onPluginDisable(PluginDisableEvent e) {
		if(e.getPlugin().getName().equals("WatchLogs")) {
			this.isWatchLogsEnable = false;
		}
	}
	
	@EventHandler
	public void onPluginEnable(PluginEnableEvent e) {
		if(e.getPlugin().getName().equals("WatchLogs")) {
			this.isWatchLogsEnable = true;
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		ges.processPlayerConnection(e.getPlayer());
		new BukkitRunnable() {		
			@Override
			public void run() {
				plugin.getPlayerUtils().processPlayerVerifications(e.getPlayer());
			}
		}.runTaskLater(plugin, 20L);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		if(removeDatas) {
			ges.removeDatasInRam(e.getPlayer());
		}
		plugin.getPlayerUtils().processPlayerVerifications(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerBoosterStatusChange(DiscordBoosterStatusChangeEvent e) {
		DiscordPlayerLink link = ges.getLinkFor(e.getPlayer().getUniqueId());
	    String discordAccountName = link.getTag();
	    String discordAccountIDName = link.getDiscordId();
		if(e.isBoosting()) {
			if(!boostExecutedCommands.isEmpty()) {
				for(String command : boostExecutedCommands) {
					String finalCommand = command.replace("%player", e.getPlayer().getName());
					plugin.getLogger().info("Dispatch command /" + finalCommand + " for player " + e.getPlayer().getName() + "(Due to booster status change)");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
					DiscordRewardsCommandEvent event = new DiscordRewardsCommandEvent(e.getPlayer().getName(), "/" + finalCommand, discordAccountName, discordAccountIDName, RewardReason.BOOST_ADDING);
					Bukkit.getServer().getPluginManager().callEvent(event);
				}
			}
		} else {
			if(!unboostExecutedCommands.isEmpty()) {
				for(String command : unboostExecutedCommands) {
					String finalCommand = command.replace("%player", e.getPlayer().getName());
					plugin.getLogger().info("Dispatch command /" + finalCommand + " for player " + e.getPlayer().getName() + "(Due to booster status change)");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
					DiscordRewardsCommandEvent event = new DiscordRewardsCommandEvent(e.getPlayer().getName(), "/" + finalCommand, discordAccountName, discordAccountIDName, RewardReason.BOOST_REMOVING);
					Bukkit.getServer().getPluginManager().callEvent(event);
				}
			}
		}
	}
	
	@EventHandler
	public void rewardsCommandExecute(DiscordRewardsCommandEvent e) {
		if(isWatchLogsEnable) {
			Player p = Bukkit.getPlayer(e.getPlayerName());
			plugin.getWatchLogsAPI().addCustomLog("linkcord-rewards-command", p, plugin.getWatchLogsAPI().getFormattedLocationString(p.getLocation()), p.getWorld().getName(), "Executed command : " + e.getExecutedCommands());
		}
	}
	
	@EventHandler
	public void onDiscordLink(DiscordLinkEvent e) {
		if(isWatchLogsEnable) {
			Player p = Bukkit.getPlayer(e.getPlayerName());
			plugin.getWatchLogsAPI().addCustomLog("discord-link", p, plugin.getWatchLogsAPI().getFormattedLocationString(p.getLocation()), p.getWorld().getName(), "Discord account name : " + e.getDiscordAccount() + " | Discord account ID : " + e.getDiscordAccountId());
		}
	}
	
	@EventHandler
	public void onDiscordUnlink(DiscordUnlinkEvent e) {
		if(isWatchLogsEnable) {
			Player p = Bukkit.getPlayer(e.getPlayerName());
			plugin.getWatchLogsAPI().addCustomLog("discord-unlink", p, plugin.getWatchLogsAPI().getFormattedLocationString(p.getLocation()), p.getWorld().getName(), "Discord account name : " + e.getDiscordAccount() + " | Discord account ID : " + e.getDiscordAccountId());
		}
	}
	
	@EventHandler
	public void onDiscordAddRole(DiscordRoleAddEvent e) {
		if(isWatchLogsEnable) {
			Player p = Bukkit.getPlayer(e.getPlayerName());
			plugin.getWatchLogsAPI().addCustomLog("discord-role-add", p, plugin.getWatchLogsAPI().getFormattedLocationString(p.getLocation()), p.getWorld().getName(), "Discord account name : " + e.getDiscordAccount() + " | Discord account ID : " + e.getDiscordAccountId() + " | Role name : " + e.getRoleName());
		}
	}
	
	@EventHandler
	public void onDiscordRemoveRole(DiscordRoleRemoveEvent e) {
		if(isWatchLogsEnable) {
			Player p = Bukkit.getPlayer(e.getPlayerName());
			plugin.getWatchLogsAPI().addCustomLog("discord-role-remove", p, plugin.getWatchLogsAPI().getFormattedLocationString(p.getLocation()), p.getWorld().getName(), "Discord account name : " + e.getDiscordAccount() + " | Discord account ID : " + e.getDiscordAccountId() + " | Role name : " + e.getRoleName());
		}
	}

}
