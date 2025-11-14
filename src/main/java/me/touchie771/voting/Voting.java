package me.touchie771.voting;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import me.touchie771.voting.commands.PollCommand;
import me.touchie771.voting.utils.PollManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Voting extends JavaPlugin {

    private LiteCommands<CommandSender> liteCommands;

    @Override
    public void onEnable() {
        PollManager.initialize(this);
        this.liteCommands = LiteBukkitFactory.builder(this)
                .commands(new PollCommand())
                .build();
    }

    @Override
    public void onDisable() {
        PollManager.savePolls();
        if (this.liteCommands != null) {
            this.liteCommands.unregister();
        }
    }
}