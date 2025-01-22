package fr.Boulldogo.LinkCord.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DiscordBoosterStatusChangeEvent extends Event {
	
	public final static HandlerList handlerList  = new HandlerList();
	private final Player player;
	private final boolean isBoosting;
	
	public DiscordBoosterStatusChangeEvent(Player player, boolean isBoosting) {
		this.player = player;
		this.isBoosting = isBoosting;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public boolean isBoosting() {
		return isBoosting;
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
	
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
