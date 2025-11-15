package me.touchie771.voting.utils;

import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

/**
 * Helper class for poll command operations that provides convenient methods
 * for accessing polls with proper permission checking and error handling.
 */
public class PollCommandHelper {
    
    /**
     * Executes an action with a poll if it exists, otherwise sends an error message.
     * 
     * @param pollId the ID of the poll to find
     * @param player the player executing the command
     * @param action the action to execute with the poll and player
     */
    public static void withPoll(String pollId, Player player, BiConsumer<PollManager.Poll, Player> action) {
        PollManager.getPollById(pollId).ifPresentOrElse(poll -> action.accept(poll, player), 
            () -> MessageUtil.sendMessage(player, "<red>Poll not found: </red><white>" + pollId + "</white>"));
    }

    /**
     * Executes an action with a poll if it exists and the player is the creator,
     * otherwise sends an appropriate error message.
     * 
     * @param player the player executing the command
     * @param pollId the ID of the poll to find
     * @param action the action to execute with the poll and player
     */
    public static void requirePollOwnership(Player player, String pollId, BiConsumer<PollManager.Poll, Player> action) {
        PollManager.getPollById(pollId).map(poll -> {
            if (!poll.getCreatorId().equals(player.getUniqueId())) {
                MessageUtil.sendMessage(player, "<red>You can only manage your own polls!</red>");
                return null;
            }
            action.accept(poll, player);
            return poll;
        }).orElseGet(() -> {
            MessageUtil.sendMessage(player, "<red>Poll not found: </red><white>" + pollId + "</white>");
            return null;
        });
    }
}