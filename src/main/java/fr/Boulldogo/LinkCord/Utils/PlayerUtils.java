package fr.Boulldogo.LinkCord.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.Boulldogo.LinkCord.LinkCord;
import fr.Boulldogo.LinkCord.Events.DiscordBoosterStatusChangeEvent;
import fr.Boulldogo.LinkCord.Utils.JSON.DiscordPlayerLink;
import fr.Boulldogo.LinkCord.Utils.JSON.LinksManager;

public class PlayerUtils {
	
	private final LinkCord plugin;
	private boolean addRolesOnUnlink;
	private boolean removeRolesOnUnlink;
	
	private Map<Long, String> roles = new HashMap<>();
	private List<Long> rolesToSetOnUnlink = new ArrayList<>();
	
	public PlayerUtils(LinkCord plugin) {
		this.plugin = plugin;
		
	    if(plugin.getConfig().getBoolean("link-permissions-with-discord-roles")) {
	        for(String roleId : plugin.getConfig().getConfigurationSection("linked-roles").getKeys(false)) {
	        	if(roleId.equals("0000000000") || roleId.equals("0000000001")) return;
	            Long finalId = Long.parseLong(roleId);
	            roles.put(finalId, plugin.getConfig().getString("linked-roles." + roleId + ".required-permission"));
	        }
	    }
	    
	    for(String role : plugin.getConfig().getStringList("roles-to-set-on-unlink")) {
	    	try {
		    	rolesToSetOnUnlink.add(Long.parseLong(role));
	    	} catch(Exception e) {
	    		plugin.getLogger().info("Invalid role value for " + role + " in roles-to-set-on-unlink");
	    	}
	    }
	    
	    for(String role : plugin.getConfig().getStringList("roles-to-set-on-unlink")) {
	    	try {
		    	rolesToSetOnUnlink.add(Long.parseLong(role));
	    	} catch(Exception e) {
	    		plugin.getLogger().info("Invalid role value for " + role + " in roles-to-set-on-unlink");
	    	}
	    }
	    
	    addRolesOnUnlink = plugin.getConfig().getBoolean("add-specific-roles-on-unlink");
	    removeRolesOnUnlink = plugin.getConfig().getBoolean("remove-player-linked-roles-on-unlink");
	}
	
	public void processPlayerVerifications(Player player) {
		Bukkit.getScheduler().runTask(plugin, () -> {
			LinksManager ges = plugin.getLinksManager();
			if(ges.isPlayerLinked(player.getUniqueId())) {
				if(!roles.isEmpty()) {
					for(Long role : roles.keySet()) {
						if(player.hasPermission(roles.get(role))) {
			                plugin.checkAndAddDiscordRole(player, role);
						} else {
			                plugin.checkAndDeleteDiscordRole(player, role);
						}
					}
				}
			    DiscordPlayerLink link = ges.getLinkFor(player.getUniqueId());
			    boolean isBoosting = plugin.playerIsBooster(player);
			    
			    if(isBoosting && !link.isBoosting()) {
			    	link.setBoosting(true);
			    	DiscordBoosterStatusChangeEvent event = new DiscordBoosterStatusChangeEvent(player, true);
			    	Bukkit.getServer().getPluginManager().callEvent(event);
			    } else if(!isBoosting && link.isBoosting()) {
			    	link.setBoosting(false);
			    	DiscordBoosterStatusChangeEvent event = new DiscordBoosterStatusChangeEvent(player, false);
			    	Bukkit.getServer().getPluginManager().callEvent(event);
			    }
			}
		});
	}

	public void removeAllLinkedRoles(OfflinePlayer player) {        
	    if(addRolesOnUnlink) {
	    	if(!rolesToSetOnUnlink.isEmpty()) {
	    		rolesToSetOnUnlink.forEach(role -> {
	                plugin.checkAndAddDiscordRole(player, role);
	    		});
	    	}
	    }

	    if(removeRolesOnUnlink) {
	    	roles.keySet().forEach(role -> {
	            plugin.checkAndDeleteDiscordRole(player, role);
	    	});
	    }
	}
}
