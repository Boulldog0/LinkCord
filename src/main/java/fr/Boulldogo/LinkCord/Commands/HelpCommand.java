package fr.Boulldogo.LinkCord.Commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.Boulldogo.LinkCord.Main;
import net.md_5.bungee.api.ChatColor;

public class HelpCommand implements CommandExecutor {
	
	private final Main plugin;
	
	public HelpCommand(Main plugin) {
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
		
		List<String> helpList = plugin.getConfig().getStringList("help-cmds-lines");
		for(String help : helpList) {
			player.sendMessage(prefix + translateString(help));
		}
		return false;
	}
	
    public String translateString(String s) {
    	return ChatColor.translateAlternateColorCodes('&', s);
    }

}
