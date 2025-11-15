package me.touchie771.voting.commands;

import me.touchie771.voting.commands.poll.*;
import me.touchie771.voting.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class PollCommand implements CommandExecutor, TabCompleter {
    
    private static final List<String> SUBCOMMANDS = Arrays.asList("create", "start", "stop", "vote", "unvote", "stats", "list");
    
    private final Map<String, PollSubcommand> subcommands = new HashMap<>();
    
    public PollCommand() {
        registerSubcommands();
    }
    
    private void registerSubcommands() {
        subcommands.put("create", new CreatePollSubcommand());
        subcommands.put("start", new StartPollSubcommand());
        subcommands.put("stop", new StopPollSubcommand());
        subcommands.put("vote", new VotePollSubcommand());
        subcommands.put("unvote", new UnvotePollSubcommand());
        subcommands.put("stats", new StatsPollSubcommand());
        subcommands.put("list", new ListPollSubcommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("voting.poll")) {
            MessageUtil.sendMessage(player, "<red>You don't have permission to use this command.</red>");
            return true;
        }
        
        if (args.length == 0) {
            MessageUtil.sendMessage(player, "<red>Usage: /poll <create|start|stop|vote|unvote|stats|list></red>");
            return true;
        }
        
        String subcommand = args[0].toLowerCase();
        PollSubcommand handler = subcommands.get(subcommand);
        
        if (handler == null) {
            MessageUtil.sendMessage(player, "<red>Unknown subcommand: </red><white>" + subcommand + "</white>");
            MessageUtil.sendMessage(player, "<red>Usage: /poll <create|start|stop|vote|unvote|stats|list></red>");
            return true;
        }
        
        return handler.execute(player, args);
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
        
        if (args.length >= 2) {
            String subcommand = args[0].toLowerCase();
            PollSubcommand handler = subcommands.get(subcommand);
            
            if (handler != null) {
                String[] subcommandArgs = Arrays.copyOfRange(args, 1, args.length);
                return handler.getTabCompletions(player, subcommandArgs);
            }
        }
        
        return new ArrayList<>();
    }
}