package me.touchie771.voting.commands;

import me.touchie771.voting.utils.PollManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PollCommand implements CommandExecutor, TabCompleter {
    
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final List<String> SUBCOMMANDS = Arrays.asList("create", "start", "stop", "vote", "unvote", "stats", "list");
    
    private record ParseResult(String value, int argsConsumed) {}
    
    private void sendMessage(Player player, String message) {
        player.sendMessage(MINI_MESSAGE.deserialize(message));
    }
    
    private ParseResult parseQuotedArgument(String[] args, int startIndex) {
        if (startIndex >= args.length) return new ParseResult("", 0);
        
        StringBuilder result = new StringBuilder();
        boolean inQuotes = false;
        int argsConsumed = 0;

        for (int i = startIndex; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.startsWith("\"") && !inQuotes) {
                inQuotes = true;
                result.append(arg.substring(1));
                argsConsumed++;
            } else if (arg.endsWith("\"") && inQuotes) {
                result.append(" ").append(arg, 0, arg.length() - 1);
                argsConsumed++;
                break;
            } else if (inQuotes) {
                result.append(" ").append(arg);
                argsConsumed++;
            } else {
                return new ParseResult(arg, 1);
            }
        }
        
        return new ParseResult(result.toString(), argsConsumed);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("voting.poll")) {
            sendMessage(player, "<red>You don't have permission to use this command.</red>");
            return true;
        }
        
        if (args.length == 0) {
            sendMessage(player, "<red>Usage: /poll <create|start|stop|vote|unvote|stats|list></red>");
            return true;
        }
        
        String subcommand = args[0].toLowerCase();
        
        switch (subcommand) {
            case "create":
                return handleCreate(player, args);
            case "start":
                return handleStart(player, args);
            case "stop":
                return handleStop(player, args);
            case "vote":
                return handleVote(player, args);
            case "unvote":
                return handleUnvote(player, args);
            case "stats":
                return handleStats(player, args);
            case "list":
                return handleList(player);
            default:
                sendMessage(player, "<red>Unknown subcommand: </red><white>" + subcommand + "</red>");
                sendMessage(player, "<red>Usage: /poll <create|start|stop|vote|unvote|stats|list></red>");
                return true;
        }
    }
    
    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 4) {
            sendMessage(player, "<red>Usage: /poll create <id> \"question\" \"option1, option2, option3\"</red>");
            sendMessage(player, "<gray>ID must be 3-16 characters: lowercase letters, numbers, underscores, and hyphens only</gray>");
            return true;
        }
        
        String pollId = args[1];
        ParseResult questionResult = parseQuotedArgument(args, 2);
        String question = questionResult.value().trim();
        
        if (question.isEmpty()) {
            sendMessage(player, "<red>Question cannot be empty!</red>");
            sendMessage(player, "<gray>Usage: /poll create <id> \"question\" \"option1, option2, option3\"</gray>");
            return true;
        }
        
        int optionsStartIndex = 2 + questionResult.argsConsumed();
        if (optionsStartIndex >= args.length) {
            sendMessage(player, "<red>Missing options argument!</red>");
            sendMessage(player, "<gray>Usage: /poll create <id> \"question\" \"option1, option2, option3\"</gray>");
            return true;
        }
        
        ParseResult optionsResult = parseQuotedArgument(args, optionsStartIndex);
        String optionsStr = optionsResult.value().trim();
        
        if (optionsStr.isEmpty()) {
            sendMessage(player, "<red>Options cannot be empty!</red>");
            sendMessage(player, "<gray>Usage: /poll create <id> \"question\" \"option1, option2, option3\"</gray>");
            return true;
        }
        
        List<String> optionList = Arrays.stream(optionsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        
        if (optionList.size() < 2) {
            sendMessage(player, "<red>Poll must have at least 2 options separated by commas!</red>");
            sendMessage(player, "<gray>Usage: /poll create <id> \"question\" \"option1, option2, option3\"</gray>");
            sendMessage(player, "<gray>ID must be 3-16 characters: lowercase letters, numbers, underscores, and hyphens only</gray>");
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

            sendMessage(player, "<green>Poll created successfully!</green>");
            sendMessage(player, "<gray>Poll ID: </gray><white>" + poll.getCustomId() + "</white>");
            sendMessage(player, "<gray>Question: </gray><white>" + question + "</white>");
            sendMessage(player, "<gray>Use </gray><yellow>/poll start " + poll.getCustomId() + "</yellow><gray> to begin voting.</gray>");
        } catch (IllegalArgumentException e) {
            sendMessage(player, "<red>Error: </red><white>" + e.getMessage() + "</white>");
        }
        
        return true;
    }
    
    private boolean handleStart(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(player, "<red>Usage: /poll start <pollId></red>");
            return true;
        }
        
        String pollId = args[1];
        withPollAccess(player, pollId, (poll, p) -> {
            if (PollManager.startPoll(pollId)) {
                sendMessage(p, "<green>Poll started: </green><white>" + pollId + "</white>");
            } else {
                sendMessage(p, "<red>Failed to start poll: </red><white>" + pollId + "</white>");
            }
        });
        
        return true;
    }
    
    private boolean handleStop(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(player, "<red>Usage: /poll stop <pollId></red>");
            return true;
        }
        
        String pollId = args[1];
        withPollAccess(player, pollId, (poll, p) -> {
            if (PollManager.stopPoll(pollId)) {
                sendMessage(p, "<green>Poll stopped: </green><white>" + pollId + "</white>");
            } else {
                sendMessage(p, "<red>Failed to stop poll: </red><white>" + pollId + "</white>");
            }
        });
        
        return true;
    }
    
    private boolean handleVote(Player player, String[] args) {
        if (args.length < 3) {
            sendMessage(player, "<red>Usage: /poll vote <pollId> <optionNumber></red>");
            return true;
        }
        
        String pollId = args[1];
        int optionNumber;
        
        try {
            optionNumber = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sendMessage(player, "<red>Option number must be a valid integer!</red>");
            return true;
        }
        
        if (optionNumber < 1) {
            sendMessage(player, "<red>Option number must be positive!</red>");
            return true;
        }
        
        int optionIndex = optionNumber - 1;
        
        withPoll(pollId, player, (poll, p) -> {
            if (!poll.isActive()) {
                sendMessage(p, "<red>This poll is not active!</red>");
                return;
            }
            
            if (poll.hasVoted(p)) {
                sendMessage(p, "<yellow>You already voted! Your vote has been changed.</yellow>");
            }
            
            if (PollManager.vote(pollId, p, optionIndex)) {
                sendMessage(p, "<green>Vote cast for option </green><white>" + optionNumber + "</white>");
            } else {
                sendMessage(p, "<red>Invalid option number!</red>");
            }
        });
        
        return true;
    }
    
    private boolean handleUnvote(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(player, "<red>Usage: /poll unvote <pollId></red>");
            return true;
        }
        
        String pollId = args[1];
        withPoll(pollId, player, (poll, p) -> {
            if (PollManager.unvote(pollId, p)) {
                sendMessage(p, "<green>Your vote has been removed.</green>");
            } else {
                sendMessage(p, "<red>You haven't voted in this poll.</red>");
            }
        });
        
        return true;
    }
    
    private boolean handleStats(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(player, "<red>Usage: /poll stats <pollId></red>");
            return true;
        }
        
        String pollId = args[1];
        withPoll(pollId, player, (poll, p) -> {
            sendMessage(p, "<gold>=== Poll Stats ===</gold>");
            sendMessage(p, "<gray>ID: </gray><white>" + poll.getShortId() + "</white>");
            if (poll.getCustomId() != null) {
                sendMessage(p, "<gray>Custom ID: </gray><white>" + poll.getCustomId() + "</white>");
            }
            sendMessage(p, "<gray>Question: </gray><white>" + poll.getName() + "</white>");
            sendMessage(p, "<gray>Creator: </gray><white>" + poll.getCreatorName() + "</white>");
            sendMessage(p, "<gray>Status: </gray>" + (poll.isActive() ? "<green>Active</green>" : "<red>Inactive</red>"));
            
            if (poll.isActive()) {
                long timeLeft = (poll.getEndTime() - System.currentTimeMillis()) / 1000;
                sendMessage(p, "<gray>Time left: </gray><white>" + timeLeft + " seconds</white>");
            }
            
            sendMessage(p, "<gray>Options:</gray>");
            for (int i = 0; i < poll.getOptions().size(); i++) {
                int votes = poll.getVoteCount(i);
                String option = poll.getOptions().get(i);
                sendMessage(p, "<white>" + (i + 1) + ". </white><gray>" + option + " </gray><dark_gray>(</dark_gray><white>" + votes + " votes</white><dark_gray>)</dark_gray>");
            }
            
            sendMessage(p, "<gray>Total votes: </gray><white>" + poll.getVotes().size() + "</white>");
        });
        
        return true;
    }
    
    private boolean handleList(Player player) {
        List<PollManager.Poll> playerPolls = PollManager.getPollsByCreator(player);
        
        if (playerPolls.isEmpty()) {
            sendMessage(player, "<gray>You haven't created any polls.</gray>");
            return true;
        }
        
        sendMessage(player, "<gold>=== Your Polls ===</gold>");
        for (PollManager.Poll poll : playerPolls) {
            String status = poll.isActive() ? "<green>Active</green>" : "<red>Inactive</red>";
            sendMessage(player, "<gray>" + poll.getShortId() + " - </gray>" + status + "<gray> - </gray><white>" + poll.getName() + "</white>");
        }
        
        return true;
    }

    private void withPoll(String pollId, Player player, java.util.function.BiConsumer<PollManager.Poll, Player> action) {
        PollManager.getPollById(pollId).ifPresentOrElse(poll -> action.accept(poll, player), 
            () -> sendMessage(player, "<red>Poll not found: </red><white>" + pollId + "</white>"));
    }

    private void withPollAccess(Player player, String pollId, java.util.function.BiConsumer<PollManager.Poll, Player> action) {
        PollManager.getPollById(pollId).map(poll -> {
            if (!poll.getCreatorId().equals(player.getUniqueId())) {
                sendMessage(player, "<red>You can only manage your own polls!</red>");
                return null;
            }
            action.accept(poll, player);
            return poll;
        }).orElseGet(() -> {
            sendMessage(player, "<red>Poll not found: </red><white>" + pollId + "</white>");
            return null;
        });
    }
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (!(sender instanceof Player player)) return new ArrayList<>();

        if (!player.hasPermission("voting.poll")) return new ArrayList<>();
        
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            if (subcommand.equals("start") || subcommand.equals("stop") || 
                subcommand.equals("vote") || subcommand.equals("unvote") || 
                subcommand.equals("stats")) {
                // Get all polls and extract their IDs for tab completion
                return PollManager.getPollsByCreator(player).stream()
                        .map(poll -> poll.getCustomId() != null ? poll.getCustomId() : poll.getShortId())
                        .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
}