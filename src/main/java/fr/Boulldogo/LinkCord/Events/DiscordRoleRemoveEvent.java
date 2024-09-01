package fr.Boulldogo.LinkCord.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DiscordRoleRemoveEvent extends Event {
	
	public final static HandlerList handlerList  = new HandlerList();
	private final String playerName;
	private final String roleName;
	private final String discordAccount;
	private final String discordAccountID;
	
	public DiscordRoleRemoveEvent(String playerName, String roleName, String discordAccount, String discordAccountID) {
		this.playerName = playerName;
		this.roleName = roleName;
		this.discordAccount = discordAccount;
		this.discordAccountID = discordAccountID;
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public String getRoleName() {
		return roleName;
	}
	
	public String getDiscordAccount() {
		return discordAccount;
	}
	
	public String getDiscordAccountId() {
		return discordAccountID;
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}

}


