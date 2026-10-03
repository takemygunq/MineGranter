package com.takemygunq.minegranter.commands;

import com.takemygunq.minegranter.Messages;
import com.takemygunq.minegranter.MineGranter;
import com.takemygunq.minegranter.Role;
import com.takemygunq.minegranter.TelegramNotifier.Chat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;
import java.util.Optional;

/** /staff give <player> <role>, /staff take <player>, /staff reload — works from the console too. */
public final class StaffCommand implements TabExecutor {

    private final MineGranter plugin;

    public StaffCommand(MineGranter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages msg = plugin.messages();
        if (!sender.hasPermission("minegranter.manage")) {
            msg.send(sender, "no_permission");
            return true;
        }
        String sub = args.length > 0 ? args[0].toLowerCase() : "";
        switch (sub) {
            case "give" -> {
                if (args.length < 3) {
                    msg.send(sender, "usage_staff");
                    return true;
                }
                Optional<Role> role = plugin.staff().role(args[2]);
                if (role.isEmpty()) {
                    msg.send(sender, "unknown_role", "role", args[2], "roles", String.join(", ", plugin.staff().roleIds()));
                    return true;
                }
                String player = args[1];
                plugin.staff().give(player, role.get());
                msg.send(sender, "role_given", "player", player, "role", role.get().prefix());
                Bukkit.broadcastMessage(msg.format("announce_new_staff", "player", player, "role", role.get().prefix()));
                plugin.telegram().notify(Chat.STAFF, "role_given",
                        "player", player, "role", role.get().prefix(), "sender", sender.getName());
            }
            case "take" -> {
                if (args.length < 2) {
                    msg.send(sender, "usage_staff");
                    return true;
                }
                plugin.staff().take(args[1]);
                msg.send(sender, "role_taken", "player", args[1]);
                plugin.telegram().notify(Chat.STAFF, "role_taken", "player", args[1], "sender", sender.getName());
            }
            case "reload" -> {
                plugin.reload();
                msg.send(sender, "reloaded");
            }
            default -> msg.send(sender, "usage_staff");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("minegranter.manage")) return List.of();
        if (args.length == 1) return Completions.matching(args[0], List.of("give", "take", "reload"));
        if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) return Completions.onlinePlayers(args[1]);
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) return Completions.matching(args[2], plugin.staff().roleIds());
        return List.of();
    }
}
