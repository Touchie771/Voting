package me.touchie771.voting.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.rollczi.litecommands.annotations.quoted.Quoted;
import me.touchie771.voting.utils.PollManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

@Command(name = "poll")
@Permission("voting.poll")
public class PollCommand {
    
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    
    private void sendMessage(Player player, String message) {
        player.sendMessage(MINI_MESSAGE.deserialize(message));
    }

    @Execute(name = "create")
    public void create(@Context Player player, @Arg @Quoted String question, @Arg @Quoted String options) {
        List<String> optionList = Arrays.stream(options.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        
        if (optionList.size() < 2) {
            sendMessage(player, "<red>Poll must have at least 2 options separated by commas!</red>");
            sendMessage(player, "<gray>Usage: /poll create \"question\" \"option1, option2, option3\"</gray>");
            return;
        }

        try {
            PollManager.Poll poll = PollManager.getBuilder()
                    .name(question)
                    .duration(5 * 60 * 1000L)
                    .creator(player)
                    .options(optionList)
                    .build();

            sendMessage(player, "<green>Poll created successfully!</green>");
            sendMessage(player, "<gray>Poll ID: </gray><white>" + poll.getShortId() + "</white>");
            sendMessage(player, "<gray>Question: </gray><white>" + question + "</white>");
            sendMessage(player, "<gray>Use </gray><yellow>/poll start " + poll.getShortId() + "</yellow><gray> to begin voting.</gray>");
        } catch (IllegalArgumentException e) {
            sendMessage(player, "<red>Error: </red><white>" + e.getMessage() + "</white>");
        }
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

    @Execute(name = "start")
    public void start(@Context Player player, @Arg String pollId) {
        withPollAccess(player, pollId, (poll, p) -> {
            if (PollManager.startPoll(pollId)) {
                sendMessage(p, "<green>Poll started: </green><white>" + pollId + "</white>");
            } else {
                sendMessage(p, "<red>Failed to start poll: </red><white>" + pollId + "</white>");
            }
        });
    }

    @Execute(name = "stop")
    public void stop(@Context Player player, @Arg String pollId) {
        withPollAccess(player, pollId, (poll, p) -> {
            if (PollManager.stopPoll(pollId)) {
                sendMessage(p, "<green>Poll stopped: </green><white>" + pollId + "</white>");
            } else {
                sendMessage(p, "<red>Failed to stop poll: </red><white>" + pollId + "</white>");
            }
        });
    }

    @Execute(name = "vote")
    public void vote(@Context Player player, @Arg String pollId, @Arg int optionNumber) {
        if (optionNumber < 1) {
            sendMessage(player, "<red>Option number must be positive!</red>");
            return;
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
    }

    @Execute(name = "unvote")
    public void unvote(@Context Player player, @Arg String pollId) {
        withPoll(pollId, player, (poll, p) -> {
            if (PollManager.unvote(pollId, p)) {
                sendMessage(p, "<green>Your vote has been removed.</green>");
            } else {
                sendMessage(p, "<red>You haven't voted in this poll.</red>");
            }
        });
    }

    @Execute(name = "stats")
    public void stats(@Context Player player, @Arg String pollId) {
        withPoll(pollId, player, (poll, p) -> {
            sendMessage(p, "<gold>=== Poll Stats ===</gold>");
            sendMessage(p, "<gray>ID: </gray><white>" + poll.getShortId() + "</white>");
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
    }

    @Execute
    public void list(@Context Player player) {
        List<PollManager.Poll> playerPolls = PollManager.getPollsByCreator(player);
        
        if (playerPolls.isEmpty()) {
            sendMessage(player, "<gray>You haven't created any polls.</gray>");
            return;
        }
        
        sendMessage(player, "<gold>=== Your Polls ===</gold>");
        for (PollManager.Poll poll : playerPolls) {
            String status = poll.isActive() ? "<green>Active</green>" : "<red>Inactive</red>";
            sendMessage(player, "<gray>" + poll.getShortId() + " - </gray>" + status + "<gray> - </gray><white>" + poll.getName() + "</white>");
        }
    }

    @Execute(name = "active")
    public void listActive(@Context Player player) {
        List<PollManager.Poll> activePolls = PollManager.getActivePolls();
        
        if (activePolls.isEmpty()) {
            sendMessage(player, "<gray>No active polls on the server.</gray>");
            return;
        }
        
        sendMessage(player, "<gold>=== Active Server Polls ===</gold>");
        for (PollManager.Poll poll : activePolls) {
            long timeLeft = (poll.getEndTime() - System.currentTimeMillis()) / 1000;
            sendMessage(player, "<gray>" + poll.getShortId() + " - </gray><white>" + poll.getName() + "</white><dark_gray> (</dark_gray><white>" + timeLeft + "s left</white><dark_gray>)</dark_gray>");
        }
    }
}