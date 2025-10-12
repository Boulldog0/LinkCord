package fr.Boulldogo.LinkCord.Commands;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import fr.Boulldogo.LinkCord.LinkCord;
import fr.Boulldogo.LinkCord.Events.DiscordRewardsCommandEvent;
import fr.Boulldogo.LinkCord.Events.RewardReason;
import fr.Boulldogo.LinkCord.Utils.JSON.DiscordPlayerLink;
import fr.Boulldogo.LinkCord.Utils.JSON.LinksManager;
import net.md_5.bungee.api.ChatColor;

public class BoosterCommand implements CommandExecutor, TabCompleter {
	
	private final LinkCord plugin;
	
	private List<String> boosterCommands = new ArrayList<>();
	
	private boolean boosterRewardsEnable;
	private int boosterRewardCooldown;
	
	public BoosterCommand(LinkCord plugin) {
		this.plugin = plugin;
		
		boosterCommands = plugin.getConfig().getStringList("executed-commands-for-booster-rewards");
		boosterRewardsEnable = plugin.getConfig().getBoolean("enable-booster-rewards");
		if(boosterRewardsEnable) {
			boosterRewardCooldown = plugin.getConfig().getInt("boost-rewards-command-cooldown");
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	    if(!(sender instanceof Player)) {
	        sender.sendMessage("Only online players can use that command!");
	        return true;
	    }

	    String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
	    
	    Player player =(Player) sender;
	    LinksManager ges = plugin.getLinksManager();

	    if(args.length < 1) {
		    if(!ges.isPlayerLinked(player.getUniqueId())) {
		        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-not-linked")));
		        return true;
		    }
		    
		    DiscordPlayerLink link = ges.getLinkFor(player.getUniqueId());

		    if(!link.isBoosting()) {
		        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you-are-not-a-booster")));
		        return true;
		    } else {
		        if(!plugin.playerIsBooster(player)) {
		            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you-are-not-a-booster")));
		            return true;
		        }
		    }

		    if(!boosterRewardsEnable) {
		        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.booster-rewards-disable")));
		        return true;
		    }
		    
		    UUID playerUUID = player.getUniqueId();

		    long currentTime = System.currentTimeMillis();
		    
		    if(!ges.playerHasBoosterCooldown(playerUUID)) {
		        long newExpireTime = currentTime + (boosterRewardCooldown * 1000);
		        ges.setPlayerBoosterCooldown(playerUUID, newExpireTime);
		        processBoosterRewardsGive(player);
		        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.rewards-correctly-given")));
		    } else {
		    	long expireTimestamp = link.getExpireBoosterCooldown();
		        long timeRemaining = expireTimestamp - currentTime;
		        String formattedTime = plugin.formatTime(timeRemaining / 1000);
		        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.booster-rewards-cooldown").replace("%t", formattedTime)));
		    }
		    return true;
	    } else {
	    	if(!player.hasPermission("linkcord.booster-reset-cooldown")) {
	            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.no-permission")));
	            return true;
	    	}
	    	
	    	if(args.length < 2) {
	            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.usage-booster")));
	            return true;
	    	}
	    	
	    	if(!args[0].equals("resetcd")) {
	            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.usage-booster")));
	            return true;
	    	}
	    	
	    	String playerName = args[1];
	    	@SuppressWarnings("deprecation")
			OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
	    	
	    	if(p == null) {
	            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.invalid-player")));
	            return true;
	    	}
	    	
	    	UUID playerUUId = p.getUniqueId();
	    	
	    	if(!ges.isPlayerLinked(playerUUId)) {
	            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-not-linked")));
	            return true;
	    	}
	    	
	    	if(!ges.playerHasBoosterCooldown(playerUUId)) {
	            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.player-has-not-cd")));
	            return true;
	    	}  	
		    long currentTimestamp = Instant.now().getEpochSecond();
	    	
	    	if(!ges.playerHasBoosterCooldown(playerUUId)) {
	            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.player-cd-already-expired")));
	            return true;
	    	}
	    	
	    	ges.setPlayerBoosterCooldown(playerUUId, currentTimestamp);
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cd-correctly-reset")));
            return true;
	    }
	}

	public void processBoosterRewardsGive(Player player) {
	    LinksManager ges = plugin.getLinksManager();
	    
	    DiscordPlayerLink link = ges.getLinkFor(player.getUniqueId());
	    if(link == null) return;
	    
	    String discordAccountName = link.getTag();
	    String discordAccountIDName = link.getDiscordId();
	    
	    if(!boosterCommands.isEmpty()) {
	    	boosterCommands.forEach(command -> {
				String finalCommand = command.replace("%player", player.getName());
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
				plugin.getLogger().info("Dispatch command /" + finalCommand + " for player " + player.getName() + "(Due to booster rewards)");
	            DiscordRewardsCommandEvent event = new DiscordRewardsCommandEvent(player.getName(),"/" + finalCommand, discordAccountName, discordAccountIDName, RewardReason.BOOSTER_COMMAND);
	            Bukkit.getPluginManager().callEvent(event);
	    	});
	    }
	}

    public String translateString(String s) {
    	return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
	    List<String> completions = new ArrayList<>();

	    if(!(sender instanceof Player)) {
	        return null;
	    }

	    if(args.length == 1) {
	        completions.add("resetcd");
	    } 

	    else if(args.length == 2 && args[0].equalsIgnoreCase("resetcd")) {
	        for(Player player : Bukkit.getOnlinePlayers()) {
	            completions.add(player.getName());
	        }
	    }

	    return completions.stream()
	        .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
	        .collect(Collectors.toList());
    }
}
