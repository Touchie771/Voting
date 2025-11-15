package me.touchie771.voting.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

/**
 * Utility class for handling messages sent to players using MiniMessage formatting.
 */
public class MessageUtil {
    
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    
    /**
     * Sends a formatted message to a player using MiniMessage.
     * 
     * @param player the player to send the message to
     * @param message the message to send (can contain MiniMessage formatting)
     */
    public static void sendMessage(Player player, String message) {
        player.sendMessage(MINI_MESSAGE.deserialize(message));
    }
}
