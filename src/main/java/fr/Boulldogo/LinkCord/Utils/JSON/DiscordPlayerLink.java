package fr.Boulldogo.LinkCord.Utils.JSON;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DiscordPlayerLink {
	
	private transient Player player;
	
	private UUID playerUUID;
	private String discordId;
	private String discordTag;
	private boolean isBoosting;
	private Long linkTime;
	private Long expireBoosterCooldown;
	
	public DiscordPlayerLink(Player player) {
		this.linkTime = System.currentTimeMillis();
		this.playerUUID = player.getUniqueId();
	}
	
	public DiscordPlayerLink() {}
	
	public void init() {
		this.player = Bukkit.getPlayer(playerUUID);
	}
	
	public void setDiscordId(String id) {
		this.discordId = id;
	}
	
	public void setBoosting(boolean boosting) {
		this.isBoosting = boosting;
	}
	
	public void setDiscordTag(String tag) {
		this.discordTag = tag;
	}
	
	public void setBoosterCooldownExpireTime(Long end) {
		this.expireBoosterCooldown = end;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public UUID getPlayerUUID() {
		return playerUUID;
	}
	
	public boolean isBoosting() {
		return isBoosting;
	}
	
	public String getDiscordId() {
		return discordId;
	}
	
	public String getTag() {
		return discordTag;
	}
	
	public Long getLinkTime() {
		return linkTime;
	}
	
	public Long getExpireBoosterCooldown() {
		return expireBoosterCooldown;
	}
}
