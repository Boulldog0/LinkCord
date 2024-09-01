package fr.Boulldogo.LinkCord.Events;

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DiscordUnlinkEvent extends Event {
	
	public final static HandlerList handlerList  = new HandlerList();
	private final String playerName;
	private final List<String> executedCommands;
	private final String discordAccount;
	private final String discordAccountID;
	
	public DiscordUnlinkEvent(String playerName, List<String> executedCommands, String discordAccount, String discordAccountID) {
		this.playerName = playerName;
		this.executedCommands = executedCommands;
		this.discordAccount = discordAccount;
		this.discordAccountID = discordAccountID;
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public List<String> getExecutedCommands() {
		return executedCommands;
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

