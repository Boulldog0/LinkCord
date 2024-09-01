package fr.Boulldogo.LinkCord.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.Boulldogo.LinkCord.Main;
import fr.Boulldogo.LinkCord.Utils.LinkCodeUtils;
import fr.Boulldogo.LinkCord.Utils.YamlFileGestionnary;
import net.md_5.bungee.api.ChatColor;

public class LinkCommand implements CommandExecutor {
	
	private final Main plugin;
	
	public LinkCommand(Main plugin) {
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
		
		if(!player.hasPermission("linkcord.link")) {
			player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.no-permission")));
			return true;
		}
		
		YamlFileGestionnary ges = plugin.getYamlGestionnary();
		
		if(ges.playerExists(player.getUniqueId())) {
			player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.account-already-linked")));
			return true;
		}
		
		LinkCodeUtils utils = plugin.getCodeUtils();
		int code = utils.getPlayerCode(player);
		
		player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.give-code")
				.replace("%code", String.valueOf(code))));
		return false;
	}

    public String translateString(String s) {
    	return ChatColor.translateAlternateColorCodes('&', s);
    }
}
