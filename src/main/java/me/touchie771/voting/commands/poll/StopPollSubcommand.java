package me.touchie771.voting.commands.poll;

import me.touchie771.voting.utils.PollManager;
import me.touchie771.voting.utils.MessageUtil;
import me.touchie771.voting.utils.PollCommandHelper;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /poll stop subcommand.
 * Usage: /poll stop <pollId>
 */
public class StopPollSubcommand implements PollSubcommand {
    
    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendMessage(player, "<red>Usage: /poll stop <pollId></red>");
            return true;
        }
        
        String pollId = args[1];
        PollCommandHelper.requirePollOwnership(player, pollId, (poll, p) -> {
            if (PollManager.stopPoll(pollId)) {
                MessageUtil.sendMessage(p, "<green>Poll stopped: </green><white>" + pollId + "</white>");
            } else {
                MessageUtil.sendMessage(p, "<red>Failed to stop poll: </red><white>" + pollId + "</white>");
            }
        });
        
        return true;
    }
    
    @Override
    public String getUsage() {
        return "/poll stop <pollId>";
    }
    
    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return PollManager.getPollsByCreator(player).stream()
                    .filter(PollManager.Poll::isActive)
                    .map(poll -> poll.getCustomId() != null ? poll.getCustomId() : poll.getShortId())
                    .filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}