package me.touchie771.voting.commands.poll;

import me.touchie771.voting.utils.PollManager;
import me.touchie771.voting.utils.MessageUtil;
import me.touchie771.voting.utils.PollCommandHelper;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /poll stats subcommand.
 * Usage: /poll stats <pollId>
 */
public class StatsPollSubcommand implements PollSubcommand {
    
    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendMessage(player, "<red>Usage: /poll stats <pollId></red>");
            return true;
        }
        
        String pollId = args[1];
        PollCommandHelper.withPoll(pollId, player, (poll, p) -> {
            MessageUtil.sendMessage(p, "<gold>=== Poll Stats ===</gold>");
            MessageUtil.sendMessage(p, "<gray>ID: </gray><white>" + poll.getShortId() + "</white>");
            if (poll.getCustomId() != null) {
                MessageUtil.sendMessage(p, "<gray>Custom ID: </gray><white>" + poll.getCustomId() + "</white>");
            }
            MessageUtil.sendMessage(p, "<gray>Question: </gray><white>" + poll.getName() + "</white>");
            MessageUtil.sendMessage(p, "<gray>Creator: </gray><white>" + poll.getCreatorName() + "</white>");
            MessageUtil.sendMessage(p, "<gray>Status: </gray>" + (poll.isActive() ? "<green>Active</green>" : "<red>Inactive</red>"));
            
            if (poll.isActive()) {
                long timeLeft = (poll.getEndTime() - System.currentTimeMillis()) / 1000;
                MessageUtil.sendMessage(p, "<gray>Time left: </gray><white>" + timeLeft + " seconds</white>");
            }
            
            MessageUtil.sendMessage(p, "<gray>Options:</gray>");
            for (int i = 0; i < poll.getOptions().size(); i++) {
                int votes = poll.getVoteCount(i);
                String option = poll.getOptions().get(i);
                MessageUtil.sendMessage(p, "<white>" + (i + 1) + ". </white><gray>" + option + " </gray><dark_gray>(</dark_gray><white>" + votes + " votes</white><dark_gray>)</dark_gray>");
            }
            
            MessageUtil.sendMessage(p, "<gray>Total votes: </gray><white>" + poll.getVotes().size() + "</white>");
        });
        
        return true;
    }
    
    @Override
    public String getUsage() {
        return "/poll stats <pollId>";
    }
    
    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1) {
            return PollManager.getAllPolls().stream()
                    .map(poll -> poll.getCustomId() != null ? poll.getCustomId() : poll.getShortId())
                    .filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
