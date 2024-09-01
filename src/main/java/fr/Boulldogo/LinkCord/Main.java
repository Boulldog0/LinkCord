package fr.Boulldogo.LinkCord;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import javax.security.auth.login.LoginException;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.Boulldogo.LinkCord.Commands.BoosterCommand;
import fr.Boulldogo.LinkCord.Commands.HelpCommand;
import fr.Boulldogo.LinkCord.Commands.LinkCommand;
import fr.Boulldogo.LinkCord.Commands.LookupCommand;
import fr.Boulldogo.LinkCord.Commands.UnlinkCommand;
import fr.Boulldogo.LinkCord.Discord.DiscordBot;
import fr.Boulldogo.LinkCord.Listeners.BaseListener;
import fr.Boulldogo.LinkCord.Utils.GithubVersion;
import fr.Boulldogo.LinkCord.Utils.LinkCodeUtils;
import fr.Boulldogo.LinkCord.Utils.PlayerUtils;
import fr.Boulldogo.LinkCord.Utils.YamlFileGestionnary;
import fr.Boulldogo.LinkCord.Utils.YamlUpdater;

public class Main extends JavaPlugin {
	
	private LinkCodeUtils codeUtils;
	private YamlFileGestionnary file;
	private DiscordBot bot;
	private PlayerUtils playerUtils;
	
	public void onEnable() {
		saveDefaultConfig();
		this.codeUtils = new LinkCodeUtils(this);
		this.file = new YamlFileGestionnary(this);	
		this.playerUtils = new PlayerUtils(this);
		
	    new Metrics(this, 23193);
	    
	    YamlUpdater updater = new YamlUpdater(this);
	    
	    String[] fileToUpdate = {"config.yml"};
	    updater.updateYamlFiles(fileToUpdate);
	    
	    Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
	        GithubVersion versionChecker = new GithubVersion(this);
	        versionChecker.checkForUpdates();
	    });
        
        if(this.getConfig().getString("discord.bot-token").equals("TOKEN HERE")) {
        	for(int i = 0; i < 50; i++) {
    			this.getLogger().severe("PLEASE CONFIGURE THE PLUGIN BEFORE USE IT ! THINK TO CONFIGURE THE BOT TOKEN, THE DIFFERENTS ROLES ID, THE GUILD ID AND MORE.");
        	}
        	this.getServer().getPluginManager().disablePlugin(this);
        	return;
        }
		
		this.bot = new DiscordBot(this);
		try {
			bot.startBot();
			this.getLogger().info("Discord bot started with success !");
		} catch (LoginException e) {
			this.getLogger().severe("Error when trying to start the discord bot : ");
			e.printStackTrace();
			this.getLogger().severe("The plugin was disabled for reason : Discord Bot starts issue.");
			this.getPluginLoader().disablePlugin(this);
		}
		
		this.getServer().getPluginManager().registerEvents(new BaseListener(this), this);
		this.getCommand("link").setExecutor(new LinkCommand(this));
		this.getCommand("unlink").setExecutor(new UnlinkCommand(this));
		this.getCommand("booster").setExecutor(new BoosterCommand(this));
		this.getCommand("ilookup").setExecutor(new LookupCommand(this));
		this.getCommand("ihelp").setExecutor(new HelpCommand(this));
		
		this.getLogger().info("Plugin LinkCord v" + this.getDescription().getVersion() + " loaded with success !");
	}
	
	public void onDisable() {
		this.getLogger().info("Plugin LinkCord v" + this.getDescription().getVersion() + " unloaded with success !");
	}
	
	public LinkCodeUtils getCodeUtils() {
		return codeUtils;
	}
	
	public YamlFileGestionnary getYamlGestionnary() {
		return file;
	}
	
	public PlayerUtils getPlayerUtils() {
		return playerUtils;
	}
	
	public void processPlayerVerifications(Player player) {
		bot.processPlayerVerifications(player);
	}
	
	public void checkAndAddDiscordRole(OfflinePlayer player, Long roleId) {
		bot.checkAndAddRolesForPlayer(player, roleId.toString());
	}
	
	public void checkAndDeleteDiscordRole(OfflinePlayer player, Long roleId) {
		bot.checkAndRemoveRolesForPlayer(player, roleId.toString());
	}
	
	public boolean playerIsBooster(Player player) {
		return bot.playerIsBooster(player);
	}
	
	public String formatTime(long seconds) {
	    long days = seconds / 86400;
	    seconds %= 86400;
	    long hours = seconds / 3600;
	    seconds %= 3600;
	    long minutes = seconds / 60;
	    seconds %= 60;

	    StringBuilder sb = new StringBuilder();
	    if(days > 0) sb.append(days).append("d");
	    if(hours > 0) sb.append(hours).append("h");
	    if(minutes > 0) sb.append(minutes).append("m");
	    if(seconds > 0) sb.append(seconds).append("s");
	    
	    return sb.toString();
	}
	
	public String getDateByTimestamp(long timestamp) {
		SimpleDateFormat date = new SimpleDateFormat("MM-dd-yyyy:HH:mm:ss");
		Date d = Date.from(Instant.ofEpochSecond(timestamp));
		return date.format(d);
	}
}
