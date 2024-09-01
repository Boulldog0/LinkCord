package fr.Boulldogo.LinkCord.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DiscordBoostRewardsEvent extends Event {
	
	public final static HandlerList handlerList  = new HandlerList();
	private final String playerName;
	private final String executedCommands;
	private final String discordAccount;
	private final String discordAccountID;
	
	public DiscordBoostRewardsEvent(String playerName, String executedCommands, String discordAccount, String discordAccountID) {
		this.playerName = playerName;
		this.executedCommands = executedCommands;
		this.discordAccount = discordAccount;
		this.discordAccountID = discordAccountID;
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public String getExecutedCommands() {
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

