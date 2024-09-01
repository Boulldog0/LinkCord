package fr.Boulldogo.LinkCord.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.entity.Player;

import fr.Boulldogo.LinkCord.Main;

public class LinkCodeUtils {
	
	private Map<Player, Integer> playerCodes = new HashMap<>();
	private Main plugin;
	
	public LinkCodeUtils(Main plugin) {
		this.plugin = plugin;
	}
	
	public int getPlayerCode(Player player) {
		if(playerCodes.containsKey(player)) {
			return playerCodes.get(player);
		} else {
			int codeLenght = plugin.getConfig().getInt("link-code-length", 8);
			
			StringBuilder builder = new StringBuilder();
			
			for(int i = 0; i < codeLenght; i++) {
				Random random = new Random();
				int integer = random.nextInt(9);
				builder.append(integer);
			}
			
			playerCodes.put(player, Integer.parseInt(builder.toString()));
			return Integer.parseInt(builder.toString());
		}
	}
	
	public boolean isCodeExists(int code) {
		for(int value : playerCodes.values()) {
			if(value == code) {
				return true;
			}
		}
		return false;
	}
	
	public String getPlayerNameWithCode(int code) {
		for(Player player : playerCodes.keySet()) {
			if(playerCodes.get(player) == code) {
				return player.getName();
			}
		}
		return null;
	}
	
	public void removePlayer(Player player) {
		playerCodes.remove(player);
	}
	
	public boolean playerHasCode(Player player) {
		return playerCodes.containsKey(player);
	}

}
