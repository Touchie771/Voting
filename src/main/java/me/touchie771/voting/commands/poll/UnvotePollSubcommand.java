package me.touchie771.voting.commands.poll;

import me.touchie771.voting.utils.PollManager;
import me.touchie771.voting.utils.MessageUtil;
import me.touchie771.voting.utils.PollCommandHelper;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /poll unvote subcommand.
 * Usage: /poll unvote <pollId>
 */
public class UnvotePollSubcommand implements PollSubcommand {
    
    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendMessage(player, "<red>Usage: /poll unvote <pollId></red>");
            return true;
        }
        
        String pollId = args[1];
        PollCommandHelper.withPoll(pollId, player, (poll, p) -> {
            if (PollManager.unvote(pollId, p)) {
                MessageUtil.sendMessage(p, "<green>Your vote has been removed.</green>");
            } else {
                MessageUtil.sendMessage(p, "<red>You haven't voted in this poll.</red>");
            }
        });
        
        return true;
    }
    
    @Override
    public String getUsage() {
        return "/poll unvote <pollId>";
    }
    
    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return PollManager.getAllPolls().stream()
                    .filter(PollManager.Poll::isActive)
                    .map(poll -> poll.getCustomId() != null ? poll.getCustomId() : poll.getShortId())
                    .filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}