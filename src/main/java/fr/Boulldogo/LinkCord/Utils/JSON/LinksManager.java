package fr.Boulldogo.LinkCord.Utils.JSON;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import fr.Boulldogo.LinkCord.LinkCord;

public class LinksManager {

    private final LinkCord plugin;
    private File folder;
    private File historyFile;
    private Gson gson = new Gson();
    private int totalLinkedAccounts;
    
    private List<UUID> alreadyLinkeds = new ArrayList<>();
    private List<UUID> allFilesFor = new ArrayList<>();
    private Map<UUID, DiscordPlayerLink> links = new HashMap<>();
    
    private boolean toSave = false;

    public LinksManager(LinkCord plugin) {
        this.plugin = plugin;
        
        this.historyFile = new File(plugin.getDataFolder(), "links-history.json");
        try {
        	if(!historyFile.exists()) {
        		historyFile.createNewFile();
        	}
        } catch(Exception e) {
			plugin.getLogger().warning("An error occured when trying to create history file :");
			e.printStackTrace();
        }
        
		@SuppressWarnings("serial")
		Type type = new TypeToken<List<UUID>>(){}.getType();
		try(FileReader reader = new FileReader(historyFile)) {
			alreadyLinkeds = gson.fromJson(reader, type);
		} catch(Exception e) {
			plugin.getLogger().warning("An error occured when trying to restore history of links : ");
			e.printStackTrace();
		}
        
        long ms = System.currentTimeMillis();
        Bukkit.getLogger().info("Loading all players datas...");
        this.folder = new File(plugin.getDataFolder(), "links");
        if(!folder.exists()) {
        	folder.mkdir();
        } else {
    		for(File file : folder.listFiles()) {
    			if(file.getName().endsWith(".json")) {
    				totalLinkedAccounts++;
					allFilesFor.add(UUID.fromString(file.getName().replace(".json", "")));
    				try(FileReader reader = new FileReader(file)) {
    					DiscordPlayerLink link = gson.fromJson(reader, DiscordPlayerLink.class);
    					links.put(link.getPlayerUUID(), link);
    				} catch(Exception e) {
    					plugin.getLogger().warning("An error occured when trying to restore datas of file " + file.getName() + " : ");
    					e.printStackTrace();
    				}
    			}
    		}
        }
        Bukkit.getLogger().info("All players datas loaded ! " + totalLinkedAccounts + " players loaded in " + (System.currentTimeMillis() - ms) + " ms");
        
        new BukkitRunnable() {
			
			@Override
			public void run() {
				if(toSave) {
					saveLinks();
					toSave = false;
				}
			}
		}.runTaskTimerAsynchronously(plugin, 0L, plugin.getConfig().getInt("automatic-save-delay") * 20);
    }
    
    public void processPlayerConnection(Player player) {
    	UUID playerUUID = player.getUniqueId();
    	if(!links.containsKey(playerUUID)) {
    		if(allFilesFor.contains(playerUUID)) {
    			File file = new File(folder, playerUUID + ".json");
				try(FileReader reader = new FileReader(file)) {
					DiscordPlayerLink link = gson.fromJson(reader, DiscordPlayerLink.class);
					links.put(playerUUID, link);
				} catch(Exception e) {
					plugin.getLogger().warning("An error occured when trying to restore datas of file " + file.getName() + " : ");
					e.printStackTrace();
				}
    		}
    	} else {
    		links.get(playerUUID).init();
    	}
    }
    
    public void removeDatasInRam(Player player) {
    	UUID playerUUID = player.getUniqueId();
    	if(links.containsKey(playerUUID)) {
    		links.remove(playerUUID);
    	}
    }
    
    public int getLinkedAccountCount() {
    	return totalLinkedAccounts;
    }

    public void saveLinks() {
    	for(DiscordPlayerLink link : links.values()) {
    		File file = new File(folder, link.getPlayerUUID() + ".json");
    		try {
    			if(!file.exists()) {
    				file.createNewFile();
    			}
    			
    			try(FileWriter writer = new FileWriter(file)) {
    				gson.toJson(link, writer);
    			}
    		} catch(Exception e) {
				plugin.getLogger().warning("An error occured when trying to save datas of user " + link.getPlayerUUID() + " : ");
				e.printStackTrace();
    		}
    	}
    	
    	try(FileWriter writer = new FileWriter(historyFile)) {
    		gson.toJson(alreadyLinkeds, writer);
    	} catch(Exception e) {
			plugin.getLogger().warning("An error occured when trying to save links history : ");
			e.printStackTrace();
		}
    }
    
    public void savePlayer(UUID playerUUID) {
    	if(links.containsKey(playerUUID)) {
    		File file = new File(folder, playerUUID + ".json");
    		if(!file.exists()) {
        		try {
        			file.createNewFile();
        		} catch(Exception e) {
        			plugin.getLogger().warning("An error occured when trying to create file to save link datas of player " + playerUUID + " : ");
        			e.printStackTrace();
        		}
    		}
    		
			try(FileWriter writer = new FileWriter(file)) {
				gson.toJson(links.get(playerUUID), writer);
			} catch(Exception e) {
				plugin.getLogger().warning("An error occured when trying to save datas of user " + playerUUID + " : ");
				e.printStackTrace();
    		}
    	}
    }
    
    public DiscordPlayerLink composeForLookupWithDiscordId(String id) {
    	for(DiscordPlayerLink link : links.values()) {
    		if(link.getDiscordId().equals(id)) {
    			return link;
    		}
    	}
		return null;
    }
    
    public DiscordPlayerLink composeForLookupWithUUID(UUID playerUUID) {
    	if(links.containsKey(playerUUID)) {
    		return links.get(playerUUID);
    	}
		return null;
    }
    
    public void removePlayer(UUID playerUUID) {
    	totalLinkedAccounts--;
    	toSave = true;
    	if(allFilesFor.contains(playerUUID)) {
        	allFilesFor.remove(playerUUID);
    	}
    	if(links.containsKey(playerUUID)) {
    		links.remove(playerUUID);
    	}
    	try {
    		File file = new File(folder, playerUUID + ".json");
    		if(file.exists()) {
    			file.delete();
    		}
    	} catch(Exception e) {
    		plugin.getLogger().warning("An error occured when trying to delete link of player " + playerUUID + " : ");
    		e.printStackTrace();
    	}
    }
    
    public void setPlayerBoosterCooldown(UUID playerUUID, Long expireTimestamp) {
    	if(links.containsKey(playerUUID)) {
        	toSave = true;
    		links.get(playerUUID).setBoosterCooldownExpireTime(expireTimestamp);
    	}
    }
    
    public boolean playerHasBoosterCooldown(UUID playerUUID) {
    	if(links.containsKey(playerUUID)) {
        	toSave = true;
    		return links.get(playerUUID).getExpireBoosterCooldown() > System.currentTimeMillis();
    	}
    	return false;
    }
    
    public Long getMillisExpireBoosterCooldownForPlayer(UUID playerUUID) {
    	if(links.containsKey(playerUUID)) {
    		return links.get(playerUUID).getExpireBoosterCooldown();
    	}
    	return 0L;
    }

    public void registerDataForPlayer(Player player, String discordAccountId, String discordTag, boolean isBooster) {
    	toSave = true;
    	totalLinkedAccounts++;
    	allFilesFor.add(player.getUniqueId());
    	DiscordPlayerLink link = new DiscordPlayerLink(player);
    	
    	link.setDiscordId(discordAccountId);
    	link.setBoosting(isBooster);
    	link.setDiscordTag(discordTag);
    	
    	links.put(player.getUniqueId(), link);
    	savePlayer(player.getUniqueId());
    }

    public DiscordPlayerLink getLinkFor(UUID playerUUID) {
    	if(links.containsKey(playerUUID)) {
    		return links.get(playerUUID);
    	}
    	return null;
    }
    
    public boolean isPlayerLinked(UUID playerUUID) {
    	return links.containsKey(playerUUID);
    }

    public boolean playerHasAlreadyLinked(UUID playerUUID) {
    	if(alreadyLinkeds == null) {
    		alreadyLinkeds = new ArrayList<>();
    		toSave = true;
    		return false;
    	}
    	return alreadyLinkeds.contains(playerUUID);
    }

    public void addLinkedPlayer(UUID playerUUID) {
    	if(!alreadyLinkeds.contains(playerUUID)) {
    		alreadyLinkeds.add(playerUUID);
    		toSave = true;
    	}
    }
}
