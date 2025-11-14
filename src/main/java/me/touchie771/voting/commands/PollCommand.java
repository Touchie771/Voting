package me.touchie771.voting.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.rollczi.litecommands.annotations.quoted.Quoted;
import me.touchie771.voting.utils.PollManager;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

@Command(name = "poll")
@Permission("voting.poll")
public class PollCommand {

    @Execute(name = "create")
    public void create(@Context Player player, @Arg @Quoted String question, @Arg @Quoted String options) {
        List<String> optionList = Arrays.stream(options.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        
        if (optionList.size() < 2) {
            player.sendMessage("§cPoll must have at least 2 options separated by commas!");
            player.sendMessage("§7Usage: /poll create \"question\" \"option1, option2, option3\"");
            return;
        }

        try {
            PollManager.Poll poll = PollManager.getBuilder()
                    .name(question)
                    .duration(5 * 60 * 1000L)
                    .creator(player)
                    .options(optionList)
                    .build();

            player.sendMessage("§aPoll created successfully!");
            player.sendMessage("§7Poll ID: §f" + poll.getShortId());
            player.sendMessage("§7Question: §f" + question);
            player.sendMessage("§7Use §e/poll start " + poll.getShortId() + " §7to begin voting.");
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cError: " + e.getMessage());
        }
    }

    private void withPoll(String pollId, Player player, java.util.function.BiConsumer<PollManager.Poll, Player> action) {
        PollManager.getPollById(pollId).ifPresentOrElse(poll -> action.accept(poll, player), 
            () -> player.sendMessage("§cPoll not found: " + pollId));
    }

    private void withPollAccess(Player player, String pollId, java.util.function.BiConsumer<PollManager.Poll, Player> action) {
        PollManager.getPollById(pollId).map(poll -> {
            if (!poll.getCreatorId().equals(player.getUniqueId())) {
                player.sendMessage("§cYou can only manage your own polls!");
                return null;
            }
            action.accept(poll, player);
            return poll;
        }).orElseGet(() -> {
            player.sendMessage("§cPoll not found: " + pollId);
            return null;
        });
    }

    @Execute(name = "start")
    public void start(@Context Player player, @Arg String pollId) {
        withPollAccess(player, pollId, (poll, p) -> {
            if (PollManager.startPoll(pollId)) {
                p.sendMessage("§aPoll started: " + pollId);
            } else {
                p.sendMessage("§cFailed to start poll: " + pollId);
            }
        });
    }

    @Execute(name = "stop")
    public void stop(@Context Player player, @Arg String pollId) {
        withPollAccess(player, pollId, (poll, p) -> {
            if (PollManager.stopPoll(pollId)) {
                p.sendMessage("§aPoll stopped: " + pollId);
            } else {
                p.sendMessage("§cFailed to stop poll: " + pollId);
            }
        });
    }

    @Execute(name = "vote")
    public void vote(@Context Player player, @Arg String pollId, @Arg int optionNumber) {
        if (optionNumber < 1) {
            player.sendMessage("§cOption number must be positive!");
            return;
        }
        
        int optionIndex = optionNumber - 1;
        
        withPoll(pollId, player, (poll, p) -> {
            if (!poll.isActive()) {
                p.sendMessage("§cThis poll is not active!");
                return;
            }
            
            if (poll.hasVoted(p)) {
                p.sendMessage("§eYou already voted! Your vote has been changed.");
            }
            
            if (PollManager.vote(pollId, p, optionIndex)) {
                p.sendMessage("§aVote cast for option " + optionNumber);
            } else {
                p.sendMessage("§cInvalid option number!");
            }
        });
    }

    @Execute(name = "unvote")
    public void unvote(@Context Player player, @Arg String pollId) {
        withPoll(pollId, player, (poll, p) -> {
            if (PollManager.unvote(pollId, p)) {
                p.sendMessage("§aYour vote has been removed.");
            } else {
                p.sendMessage("§cYou haven't voted in this poll.");
            }
        });
    }

    @Execute(name = "stats")
    public void stats(@Context Player player, @Arg String pollId) {
        withPoll(pollId, player, (poll, p) -> {
            p.sendMessage("§6=== Poll Stats ===");
            p.sendMessage("§7ID: §f" + poll.getShortId());
            p.sendMessage("§7Question: §f" + poll.getName());
            p.sendMessage("§7Creator: §f" + poll.getCreatorName());
            p.sendMessage("§7Status: " + (poll.isActive() ? "§aActive" : "§cInactive"));
            
            if (poll.isActive()) {
                long timeLeft = (poll.getEndTime() - System.currentTimeMillis()) / 1000;
                p.sendMessage("§7Time left: §f" + timeLeft + " seconds");
            }
            
            p.sendMessage("§7Options:");
            for (int i = 0; i < poll.getOptions().size(); i++) {
                int votes = poll.getVoteCount(i);
                String option = poll.getOptions().get(i);
                p.sendMessage("§f" + (i + 1) + ". §7" + option + " §8(§f" + votes + " votes§8)");
            }
            
            p.sendMessage("§7Total votes: §f" + poll.getVotes().size());
        });
    }

    @Execute
    public void list(@Context Player player) {
        List<PollManager.Poll> playerPolls = PollManager.getPollsByCreator(player);
        
        if (playerPolls.isEmpty()) {
            player.sendMessage("§7You haven't created any polls.");
            return;
        }
        
        player.sendMessage("§6=== Your Polls ===");
        for (PollManager.Poll poll : playerPolls) {
            String status = poll.isActive() ? "§aActive" : "§cInactive";
            player.sendMessage("§7" + poll.getShortId() + " - " + status + " - " + poll.getName());
        }
    }

    @Execute(name = "active")
    public void listActive(@Context Player player) {
        List<PollManager.Poll> activePolls = PollManager.getActivePolls();
        
        if (activePolls.isEmpty()) {
            player.sendMessage("§7No active polls on the server.");
            return;
        }
        
        player.sendMessage("§6=== Active Server Polls ===");
        for (PollManager.Poll poll : activePolls) {
            long timeLeft = (poll.getEndTime() - System.currentTimeMillis()) / 1000;
            player.sendMessage("§7" + poll.getShortId() + " - " + poll.getName() + " §8(§f" + timeLeft + "s left§8)");
        }
    }
}