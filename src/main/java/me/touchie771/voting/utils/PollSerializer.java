package me.touchie771.voting.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PollSerializer {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    
    private static final Type POLL_LIST_TYPE = new TypeToken<List<PollData>>() {}.getType();
    
    private final JavaPlugin plugin;
    private final File pollsFile;
    
    public PollSerializer(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pollsFile = new File(plugin.getDataFolder(), "polls.json");
        
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Failed to create plugin data folder!");
            }
        }
    }
    
    public void savePolls(List<PollManager.Poll> polls) {
        try {
            List<PollData> pollDataList = polls.stream()
                    .map(PollData::fromPoll)
                    .toList();
            
            try (FileWriter writer = new FileWriter(pollsFile)) {
                GSON.toJson(pollDataList, writer);
            }
            
            plugin.getLogger().info("Saved " + polls.size() + " polls to file");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save polls to file", e);
        }
    }
    
    public List<PollData> loadPolls() {
        if (!pollsFile.exists()) {
            plugin.getLogger().info("No polls file found, starting with empty poll list");
            return List.of();
        }
        
        try (FileReader reader = new FileReader(pollsFile)) {
            List<PollData> pollDataList = GSON.fromJson(reader, POLL_LIST_TYPE);
            if (pollDataList == null) {
                return List.of();
            }
            
            plugin.getLogger().info("Loaded " + pollDataList.size() + " polls from file");
            return pollDataList;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load polls from file", e);
            return List.of();
        }
    }

    public record PollData(UUID id, String name, long duration, UUID creatorId, String creatorName,
                           List<String> options, Map<UUID, Integer> votes, boolean active, long startTime,
                           long endTime) {

        public static PollData fromPoll(PollManager.Poll poll) {
                return new PollData(
                        poll.getId(),
                        poll.getName(),
                        poll.getDuration(),
                        poll.getCreatorId(),
                        poll.getCreatorName(),
                        poll.getOptions(),
                        poll.getVotes(),
                        poll.isActive(),
                        poll.getStartTime(),
                        poll.getEndTime()
                );
            }

        public PollManager.Poll toPoll() {
                return new PollManager.Poll(this);
            }
        }
}