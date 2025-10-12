package fr.Boulldogo.LinkCord.Discord;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.Boulldogo.LinkCord.LinkCord;
import fr.Boulldogo.LinkCord.Utils.JSON.DiscordPlayerLink;
import fr.Boulldogo.LinkCord.Utils.JSON.LinksManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class BotExtension {
	
	private LinkCord plugin;
	private LinksManager manager;
	private JDA jda;
	
	public BotExtension(LinkCord plugin, JDA jda, LinksManager manager) {
		this.plugin = plugin;
		this.jda = jda;
		this.manager = manager;
	}

	public boolean sendDiscordMessageToPlayer(Player player, MessageCreateData message) {
	    UUID playerUUID = player.getUniqueId();

	    if (!manager.isPlayerLinked(playerUUID)) return false;

	    DiscordPlayerLink link = manager.getLinkFor(playerUUID);

	    try {
	        new BukkitRunnable() {
	            @SuppressWarnings("deprecation")
				@Override
	            public void run() {
	                jda.retrieveUserById(link.getDiscordId()).queue(user -> {
	                    user.openPrivateChannel().queue(channel -> {
	                        channel.sendMessage(message).queue();
	                    }, err -> plugin.getLogger().warning("An error occured when trying to open DM with user " + user.getAsTag() + "."));
	                }, err -> plugin.getLogger().warning("Unknown discord user for player " + link.getPlayerUUID() + " : " + err.getMessage()));
	            }
	        }.runTaskAsynchronously(plugin);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	    return true;
	}
}
