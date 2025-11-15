package me.touchie771.voting.commands.poll;

import me.touchie771.voting.utils.PollManager;
import me.touchie771.voting.utils.MessageUtil;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Handles the /poll list subcommand.
 * Usage: /poll list
 */
public class ListPollSubcommand implements PollSubcommand {
    
    @Override
    public boolean execute(Player player, String[] args) {
        List<PollManager.Poll> playerPolls = PollManager.getPollsByCreator(player);
        
        if (playerPolls.isEmpty()) {
            MessageUtil.sendMessage(player, "<gray>You haven't created any polls.</gray>");
            return true;
        }
        
        MessageUtil.sendMessage(player, "<gold>=== Your Polls ===</gold>");
        for (PollManager.Poll poll : playerPolls) {
            String status = poll.isActive() ? "<green>Active</green>" : "<red>Inactive</red>";
            MessageUtil.sendMessage(player, "<gray>" + poll.getShortId() + " - </gray>" + status + "<gray> - </gray><white>" + poll.getName() + "</white>");
        }
        
        return true;
    }
    
    @Override
    public String getUsage() {
        return "/poll list";
    }
    
    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        return List.of();
    }
}
