package me.touchie771.voting.commands.poll;

import org.bukkit.entity.Player;
import java.util.List;

/**
 * Interface for all poll subcommands.
 * Each subcommand implements this interface to provide consistent execution and tab completion.
 */
public interface PollSubcommand {
    
    /**
     * Executes the subcommand with the given arguments.
     * 
     * @param player the player executing the command
     * @param args the command arguments (excluding the subcommand name)
     * @return true if the command was handled successfully
     */
    boolean execute(Player player, String[] args);
    
    /**
     * Returns the usage string for this subcommand.
     * 
     * @return the usage string
     */
    String getUsage();
    
    /**
     * Returns tab completion suggestions for this subcommand.
     * 
     * @param player the player requesting tab completion
     * @param args the current command arguments (excluding the subcommand name)
     * @return list of tab completion suggestions
     */
    List<String> getTabCompletions(Player player, String[] args);
}
