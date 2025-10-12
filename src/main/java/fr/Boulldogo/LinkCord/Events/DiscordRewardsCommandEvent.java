package fr.Boulldogo.LinkCord.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DiscordRewardsCommandEvent extends Event {
	
	public final static HandlerList handlerList  = new HandlerList();
	private final String playerName;
	private final RewardReason reason;
	private final String executedCommands;
	private final String discordAccount;
	private final String discordAccountID;
	
	public DiscordRewardsCommandEvent(String playerName, String executedCommands, String discordAccount, String discordAccountID, RewardReason reason) {
		this.playerName = playerName;
		this.executedCommands = executedCommands;
		this.discordAccount = discordAccount;
		this.discordAccountID = discordAccountID;
		this.reason = reason;
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
	
	public RewardReason getReason() {
		return reason;
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
	
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}

