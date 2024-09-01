package fr.Boulldogo.LinkCord.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.Boulldogo.LinkCord.Main;

public class BaseListener implements Listener {
	
	private final Main plugin;
	
	public BaseListener(Main plugin) {
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

}
