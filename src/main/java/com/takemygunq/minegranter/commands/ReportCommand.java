package com.takemygunq.minegranter.commands;

import com.takemygunq.minegranter.Messages;
import com.takemygunq.minegranter.MineGranter;
import com.takemygunq.minegranter.TelegramNotifier.Chat;
import com.takemygunq.minegranter.util.Durations;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** /report <player> <reason> — sends a report to the staff's Telegram chat. */
public final class ReportCommand implements TabExecutor {

    private static final Pattern NICKNAME = Pattern.compile("[A-Za-z0-9_]{3,16}");

    private final MineGranter plugin;
    /** When each player may report again (epoch millis) */
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public ReportCommand(MineGranter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages msg = plugin.messages();
        if (!(sender instanceof Player player)) {
            msg.send(sender, "players_only");
            return true;
        }
        if (!player.hasPermission("minegranter.report")) {
            msg.send(player, "no_permission");
            return true;
        }
        long now = System.currentTimeMillis();
        long readyAt = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (readyAt > now) {
            msg.send(player, "report_cooldown", "time", Durations.format((readyAt - now + 999) / 1000));
            return true;
        }
        if (args.length < 2) {
            msg.send(player, "usage_report");
            return true;
        }
        String target = args[0];
        if (!NICKNAME.matcher(target).matches()) {
            msg.send(player, "invalid_nick");
            return true;
        }
        if (target.equalsIgnoreCase(player.getName())) {
            msg.send(player, "report_self");
            return true;
        }
        // Immunity can only be checked for online players; offline players can still be reported
        Player online = Bukkit.getPlayerExact(target);
        if (online != null && online.hasPermission("minegranter.report.immune")) {
            msg.send(player, "report_immune");
            return true;
        }
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if (reason.length() < plugin.getConfig().getInt("reports.min_reason_length", 3)) {
            msg.send(player, "report_short_reason");
            return true;
        }

        String targetName = online != null ? online.getName() : target;
        plugin.telegram().notify(Chat.REPORTS, "report",
                "reporter", player.getName(), "target", targetName, "reason", reason,
                "server", plugin.getConfig().getString("reports.server", ""));
        msg.send(player, "report_sent", "target", targetName, "reason", reason);
        long cooldown = Durations.parseSeconds(plugin.getConfig().getString("reports.cooldown", "2m"));
        cooldowns.put(player.getUniqueId(), now + cooldown * 1000);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 1 ? Completions.onlinePlayers(args[0]) : List.of();
    }
}
