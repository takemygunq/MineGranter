package com.takemygunq.minegranter.commands;

import com.takemygunq.minegranter.Messages;
import com.takemygunq.minegranter.MineGranter;
import com.takemygunq.minegranter.StaffService.Shift;
import com.takemygunq.minegranter.StaffService.Status;
import com.takemygunq.minegranter.TelegramNotifier.Chat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** /sw start, /sw stop — a staff shift; /sw check <player> — someone's staff status. */
public final class ShiftCommand implements TabExecutor {

    private final MineGranter plugin;

    public ShiftCommand(MineGranter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages msg = plugin.messages();
        String sub = args.length > 0 ? args[0].toLowerCase() : "";

        if (sub.equals("check")) {
            if (!sender.hasPermission("minegranter.shift.check")) {
                msg.send(sender, "no_permission");
                return true;
            }
            if (args.length < 2) {
                msg.send(sender, "usage_sw");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                msg.send(sender, "player_not_found", "player", args[1]);
                return true;
            }
            Optional<Status> status = plugin.staff().status(target);
            if (status.isEmpty()) {
                msg.send(sender, "check_not_staff", "player", target.getName());
            } else {
                String key = status.get().shift() == Shift.ON ? "check_on_shift" : "check_off_shift";
                msg.send(sender, key, "player", target.getName(), "role", status.get().role().prefix());
            }
            return true;
        }

        if (!sub.equals("start") && !sub.equals("stop")) {
            msg.send(sender, "usage_sw");
            return true;
        }
        if (!(sender instanceof Player player)) {
            msg.send(sender, "players_only");
            return true;
        }
        if (!player.hasPermission("minegranter.shift")) {
            msg.send(player, "no_permission");
            return true;
        }
        if (player.isOp()) {
            msg.send(player, "operator_shift");
            return true;
        }
        Optional<Status> status = plugin.staff().status(player);
        if (status.isEmpty()) {
            msg.send(player, "not_staff");
            return true;
        }
        Status s = status.get();
        if (sub.equals("start")) {
            if (s.shift() == Shift.ON) {
                msg.send(player, "already_on_shift");
                return true;
            }
            plugin.staff().startShift(player, s.role());
            msg.send(player, "shift_started");
            plugin.telegram().notify(Chat.STAFF, "shift_started", "player", player.getName(), "role", s.role().prefix());
        } else {
            if (s.shift() == Shift.OFF) {
                msg.send(player, "not_on_shift");
                return true;
            }
            plugin.staff().stopShift(player, s.role());
            msg.send(player, "shift_stopped");
            plugin.telegram().notify(Chat.STAFF, "shift_stopped", "player", player.getName(), "role", s.role().prefix());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (sender.hasPermission("minegranter.shift")) subs.addAll(List.of("start", "stop"));
            if (sender.hasPermission("minegranter.shift.check")) subs.add("check");
            return Completions.matching(args[0], subs);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("check") && sender.hasPermission("minegranter.shift.check")) {
            return Completions.onlinePlayers(args[1]);
        }
        return List.of();
    }
}
