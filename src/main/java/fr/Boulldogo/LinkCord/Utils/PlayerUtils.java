package fr.Boulldogo.LinkCord.Utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.Boulldogo.LinkCord.Main;

public class PlayerUtils {
	
	private final Main plugin;
	
	public PlayerUtils(Main plugin) {
		this.plugin = plugin;
	}
	
	public void processPlayerVerifications(Player player) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				YamlFileGestionnary ges = plugin.getYamlGestionnary();
				if(ges.playerExists(player.getUniqueId())) {
				    if(plugin.getConfig().getBoolean("link-permissions-with-discord-roles")) {
				        for(String roleId : plugin.getConfig().getConfigurationSection("linked-roles").getKeys(false)) {
				        	if(roleId.equals("0000000000") || roleId.equals("0000000001")) return;
				            Long finalId = Long.parseLong(roleId);
				            if(player.hasPermission("linked-roles." + roleId + ".required-permission")) {
				                plugin.checkAndAddDiscordRole(player, finalId);
				            } else {
				                plugin.checkAndDeleteDiscordRole(player, finalId);
				            }
				        }
				    }
				}
			}	
		}.runTaskAsynchronously(plugin);
	}

	public void removeAllLinkedRoles(OfflinePlayer player) {
	    plugin.getLogger().info("Executing removeAllLinkedRoles for player: " + player.getName());
	    
	    boolean addRolesOnUnlink = plugin.getConfig().getBoolean("add-specific-roles-on-unlink");
	    boolean removeRolesOnUnlink = plugin.getConfig().getBoolean("remove-player-linked-roles-on-unlink");
	    
	    plugin.getLogger().info("add-specific-roles-on-unlink: " + addRolesOnUnlink);
	    plugin.getLogger().info("remove-player-linked-roles-on-unlink: " + removeRolesOnUnlink);
	    
	    if(addRolesOnUnlink) {
	        plugin.getLogger().info("Unlink detected/ Set specific roles on unlink true");
	        if(!plugin.getConfig().getStringList("roles-to-set-on-unlink").isEmpty()) {
	            for(String roleId : plugin.getConfig().getStringList("roles-to-set-on-unlink")) {
	                Long finalId = Long.parseLong(roleId);
	                plugin.checkAndAddDiscordRole(player, finalId);
	                plugin.getLogger().info("Added role: " + finalId + " to player: " + player.getName());
	            }
	        } else {
	            plugin.getLogger().info("List empty!");
	        }
	    } else {
	        plugin.getLogger().info("Unlink not detected -> false");
	    }

	    if(removeRolesOnUnlink) {
	        for(String roleId : plugin.getConfig().getConfigurationSection("linked-roles").getKeys(false)) {
	            plugin.getLogger().info("Processing role ID: " + roleId);
	            if(roleId.equals("0000000000") || roleId.equals("0000000001")) {
	                plugin.getLogger().info("Skipping role ID: " + roleId);
	                continue;  
	            }
	            Long finalId = Long.parseLong(roleId);
	            plugin.checkAndDeleteDiscordRole(player, finalId);
	            plugin.getLogger().info("Removed role: " + finalId + " from player: " + player.getName());
	        }
	    } else {
	        plugin.getLogger().info("remove-player-linked-roles-on-unlink is false");
	    }
	}
}
