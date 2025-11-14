package me.touchie771.voting;

import me.touchie771.voting.commands.PollCommand;
import me.touchie771.voting.utils.PollManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Voting extends JavaPlugin {

    @Override
    public void onEnable() {
        PollManager.initialize(this);
        PollCommand pollCommand = new PollCommand();
        if (getCommand("poll") != null) {
            Objects.requireNonNull(getCommand("poll")).setExecutor(pollCommand);
            Objects.requireNonNull(getCommand("poll")).setTabCompleter(pollCommand);
        }
    }

    @Override
    public void onDisable() {
        PollManager.savePolls();
    }
}