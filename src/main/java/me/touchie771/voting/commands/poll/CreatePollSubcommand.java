package me.touchie771.voting.commands.poll;

import me.touchie771.voting.utils.PollManager;
import me.touchie771.voting.utils.ArgumentParser;
import me.touchie771.voting.utils.MessageUtil;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Handles the /poll create subcommand.
 * Usage: /poll create <id> "question" "option1, option2, option3"
 */
public class CreatePollSubcommand implements PollSubcommand {
    
    @Override
    public boolean execute(Player player, String[] args) {
        if (args.length < 4) {
            MessageUtil.sendMessage(player, "<red>Usage: /poll create <id> \"question\" \"option1, option2, option3\"</red>");
            MessageUtil.sendMessage(player, "<gray>ID must be 3-16 characters: lowercase letters, numbers, underscores, and hyphens only</gray>");
            return true;
        }
        
        String pollId = args[1];
        ArgumentParser.ParseResult questionResult = ArgumentParser.parseQuotedArgument(args, 2);
        String question = questionResult.value().trim();
        
        if (question.isEmpty()) {
            MessageUtil.sendMessage(player, "<red>Question cannot be empty!</red>");
            MessageUtil.sendMessage(player, "<gray>Usage: /poll create <id> \"question\" \"option1, option2, option3\"</gray>");
            return true;
        }
        
        int optionsStartIndex = 2 + questionResult.argsConsumed();
        if (optionsStartIndex >= args.length) {
            MessageUtil.sendMessage(player, "<red>Missing options argument!</red>");
            MessageUtil.sendMessage(player, "<gray>Usage: /poll create <id> \"question\" \"option1, option2, option3\"</gray>");
            return true;
        }
        
        ArgumentParser.ParseResult optionsResult = ArgumentParser.parseQuotedArgument(args, optionsStartIndex);
        String optionsStr = optionsResult.value().trim();
        
        if (optionsStr.isEmpty()) {
            MessageUtil.sendMessage(player, "<red>Options cannot be empty!</red>");
            MessageUtil.sendMessage(player, "<gray>Usage: /poll create <id> \"question\" \"option1, option2, option3\"</gray>");
            return true;
        }
        
        List<String> optionList = Arrays.stream(optionsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        
        if (optionList.size() < 2) {
            MessageUtil.sendMessage(player, "<red>Poll must have at least 2 options separated by commas!</red>");
            MessageUtil.sendMessage(player, "<gray>Usage: /poll create <id> \"question\" \"option1, option2, option3\"</gray>");
            MessageUtil.sendMessage(player, "<gray>ID must be 3-16 characters: lowercase letters, numbers, underscores, and hyphens only</gray>");
            return true;
        }

        try {
            PollManager.Poll poll = PollManager.getBuilder()
                    .customId(pollId)
                    .name(question)
                    .duration(5 * 60 * 1000L)
                    .creator(player)
                    .options(optionList)
                    .build();

            MessageUtil.sendMessage(player, "<green>Poll created successfully!</green>");
            MessageUtil.sendMessage(player, "<gray>Poll ID: </gray><white>" + poll.getCustomId() + "</white>");
            MessageUtil.sendMessage(player, "<gray>Question: </gray><white>" + question + "</white>");
            MessageUtil.sendMessage(player, "<gray>Use </gray><yellow>/poll start " + poll.getCustomId() + "</yellow><gray> to begin voting.</gray>");
        } catch (IllegalArgumentException e) {
            MessageUtil.sendMessage(player, "<red>Error: </red><white>" + e.getMessage() + "</white>");
        }
        
        return true;
    }
    
    @Override
    public String getUsage() {
        return "/poll create <id> \"question\" \"option1, option2, option3\"";
    }
    
    @Override
    public List<String> getTabCompletions(Player player, String[] args) {
        return List.of();
    }
}
