package me.touchie771.voting.commands.poll;

import me.touchie771.voting.utils.PollManager;
import me.touchie771.voting.utils.MessageUtil;
import me.touchie771.voting.utils.PollCommandHelper;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /poll vote subcommand.
 * Usage: /poll vote <pollId> <optionNumber>
 */
public class VotePollSubcommand implements PollSubcommand {
    
    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length < 3) {
            MessageUtil.sendMessage(player, "<red>Usage: /poll vote <pollId> <optionNumber></red>");
            return true;
        }
        
        String pollId = args[1];
        int optionNumber;
        
        try {
            optionNumber = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            MessageUtil.sendMessage(player, "<red>Option number must be a valid integer!</red>");
            return true;
        }
        
        if (optionNumber < 1) {
            MessageUtil.sendMessage(player, "<red>Option number must be positive!</red>");
            return true;
        }
        
        int optionIndex = optionNumber - 1;
        
        PollCommandHelper.withPoll(pollId, player, (poll, p) -> {
            if (!poll.isActive()) {
                MessageUtil.sendMessage(p, "<red>This poll is not active!</red>");
                return;
            }
            
            if (poll.hasVoted(p)) {
                MessageUtil.sendMessage(p, "<yellow>You already voted! Your vote has been changed.</yellow>");
            }
            
            if (PollManager.vote(pollId, p, optionIndex)) {
                MessageUtil.sendMessage(p, "<green>Vote cast for option </green><white>" + optionNumber + "</white>");
            } else {
                MessageUtil.sendMessage(p, "<red>Invalid option number!</red>");
            }
        });
        
        return true;
    }
    
    @Override
    public String getUsage() {
        return "/poll vote <pollId> <optionNumber>";
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
        if (args.length == 2) {
            String pollId = args[0];
            return PollManager.getPollById(pollId)
                    .filter(PollManager.Poll::isActive)
                    .map(poll -> {
                        List<String> options = new ArrayList<>();
                        for (int i = 0; i < poll.getOptions().size(); i++) {
                            options.add(String.valueOf(i + 1));
                        }
                        return options.stream()
                                .filter(option -> option.startsWith(args[1]))
                                .collect(Collectors.toList());
                    })
                    .orElse(List.of());
        }
        return List.of();
    }
}