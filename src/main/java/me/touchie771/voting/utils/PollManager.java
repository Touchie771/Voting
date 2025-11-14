package me.touchie771.voting.utils;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PollManager {

    private static final Set<Poll> pollSet = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Poll> pollMap = new ConcurrentHashMap<>();

    public static PollBuilder getBuilder() {
        return new PollBuilder();
    }

    public static Optional<Poll> getPollById(String pollId) {
        // Try full UUID first
        try {
            UUID uuid = UUID.fromString(pollId);
            return Optional.ofNullable(pollMap.get(uuid));
        } catch (IllegalArgumentException e) {
            // Try short ID lookup
            return pollSet.stream()
                    .filter(poll -> poll.getShortId().equals(pollId))
                    .findFirst();
        }
    }

    public static List<Poll> getPollsByCreator(Player player) {
        return pollSet.stream()
                .filter(poll -> poll.getCreatorId().equals(player.getUniqueId()))
                .toList();
    }

    public static List<Poll> getActivePolls() {
        return pollSet.stream()
                .filter(Poll::isActive)
                .toList();
    }

    public static boolean startPoll(String pollId) {
        Optional<Poll> pollOpt = getPollById(pollId);
        if (pollOpt.isPresent()) {
            pollOpt.get().start();
            return true;
        }
        return false;
    }

    public static boolean stopPoll(String pollId) {
        Optional<Poll> pollOpt = getPollById(pollId);
        if (pollOpt.isPresent()) {
            pollOpt.get().stop();
            return true;
        }
        return false;
    }

    public static boolean vote(String pollId, Player player, int optionIndex) {
        Optional<Poll> pollOpt = getPollById(pollId);
        return pollOpt.map(poll -> poll.vote(player, optionIndex)).orElse(false);
    }

    public static boolean unvote(String pollId, Player player) {
        Optional<Poll> pollOpt = getPollById(pollId);
        return pollOpt.map(poll -> poll.unvote(player)).orElse(false);
    }

    public static class PollBuilder {
        private String name;
        private long duration;
        private UUID creatorId;
        private String creatorName;
        private List<String> options = new ArrayList<>();

        public PollBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PollBuilder duration(long duration) {
            this.duration = duration;
            return this;
        }

        public PollBuilder creator(Player creator) {
            this.creatorId = creator.getUniqueId();
            this.creatorName = creator.getName();
            return this;
        }

        public PollBuilder options(List<String> options) {
            this.options = new ArrayList<>(options);
            return this;
        }

        public Poll build() {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Poll name cannot be empty");
            }
            if (options.size() < 2) {
                throw new IllegalArgumentException("Poll must have at least 2 options");
            }
            if (creatorId == null) {
                throw new IllegalArgumentException("Poll creator cannot be null");
            }
            // Check for duplicate options
            if (options.size() != new HashSet<>(options).size()) {
                throw new IllegalArgumentException("Poll options must be unique");
            }
            
            // Create poll first to get short ID
            Poll poll = getPoll();

            pollSet.add(poll);
            pollMap.put(poll.getId(), poll);
            return poll;
        }

        private @NotNull Poll getPoll() {
            Poll poll = new Poll(this);

            // Check for short ID collision
            for (Poll existingPoll : pollSet) {
                if (existingPoll.getShortId().equals(poll.getShortId())) {
                    // Collision detected, create new poll with different UUID
                    poll = new Poll(this);
                    // Check again (very unlikely to collide twice)
                    for (Poll existingPoll2 : pollSet) {
                        if (existingPoll2.getShortId().equals(poll.getShortId())) {
                            throw new RuntimeException("Unable to generate unique short ID");
                        }
                    }
                }
            }
            return poll;
        }
    }

    public static class Poll {
        private final UUID id;
        private final String name;
        private final long duration;
        private final UUID creatorId;
        private final String creatorName;
        private final List<String> options;
        private final Map<UUID, Integer> votes;
        private boolean active;
        private long startTime;
        private long endTime;

        public Poll(PollBuilder pollBuilder) {
            this.id = UUID.randomUUID();
            this.name = pollBuilder.name;
            this.duration = pollBuilder.duration;
            this.creatorId = pollBuilder.creatorId;
            this.creatorName = pollBuilder.creatorName;
            this.options = List.copyOf(pollBuilder.options);
            this.votes = new ConcurrentHashMap<>();
            this.active = false;
        }

        public void start() {
            if (!active) {
                this.active = true;
                this.startTime = System.currentTimeMillis();
                this.endTime = startTime + duration;
            }
        }

        public void stop() {
            this.active = false;
        }

        public boolean vote(Player player, int optionIndex) {
            if (!active || optionIndex < 0 || optionIndex >= options.size()) {
                return false;
            }
            votes.put(player.getUniqueId(), optionIndex);
            return true;
        }

        public boolean unvote(Player player) {
            return votes.remove(player.getUniqueId()) != null;
        }

        public boolean hasVoted(Player player) {
            return votes.containsKey(player.getUniqueId());
        }

        public int getVoteCount(int optionIndex) {
            return (int) votes.values().stream()
                    .filter(option -> option == optionIndex)
                    .count();
        }

        public boolean isExpired() {
            return active && System.currentTimeMillis() > endTime;
        }

        public String getShortId() {
            return id.toString().substring(0, 8);
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public long getDuration() {
            return duration;
        }

        public UUID getCreatorId() {
            return creatorId;
        }

        public String getCreatorName() {
            return creatorName;
        }

        public List<String> getOptions() {
            return options;
        }

        public Map<UUID, Integer> getVotes() {
            return new HashMap<>(votes);
        }

        public boolean isActive() {
            return active;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
        }
    }
}