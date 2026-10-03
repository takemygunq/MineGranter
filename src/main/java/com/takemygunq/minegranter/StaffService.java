package com.takemygunq.minegranter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Staff roles and shifts. Role membership is read from LuckPerms group permissions (group.<name>). */
public final class StaffService {

    public enum Shift { ON, OFF }

    /** A player's role and whether they are on shift. */
    public record Status(Role role, Shift shift) {
    }

    private final MineGranter plugin;
    private final Map<String, Role> roles = new LinkedHashMap<>();
    private String setGroupCommand;
    private String removeStaffCommand;
    private List<String> onStart = List.of();
    private List<String> onStop = List.of();

    StaffService(MineGranter plugin) {
        this.plugin = plugin;
    }

    void load(FileConfiguration config) {
        roles.clear();
        ConfigurationSection section = config.getConfigurationSection("roles");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection r = section.getConfigurationSection(id);
                if (r == null) continue;
                String onGroup = r.getString("on_group");
                String offGroup = r.getString("off_group");
                if (onGroup == null || offGroup == null) {
                    plugin.getLogger().warning("Role '" + id + "' needs both on_group and off_group — skipped");
                    continue;
                }
                roles.put(id.toLowerCase(Locale.ROOT), new Role(id, r.getString("prefix", id), onGroup, offGroup));
            }
        }
        setGroupCommand = config.getString("commands.set_group", "lp user {player} parent set {group}");
        removeStaffCommand = config.getString("commands.remove_staff", "lp user {player} parent set default");
        onStart = config.getStringList("shift.on_start");
        onStop = config.getStringList("shift.on_stop");
    }

    public Collection<Role> roles() {
        return Collections.unmodifiableCollection(roles.values());
    }

    public Optional<Role> role(String id) {
        return Optional.ofNullable(roles.get(id.toLowerCase(Locale.ROOT)));
    }

    /**
     * The player's staff status. Operators are never treated as staff: they implicitly have every
     * group.* permission, so they would otherwise look like members of every group.
     */
    public Optional<Status> status(Player player) {
        if (player.isOp()) return Optional.empty();
        for (Role role : roles.values()) {
            if (player.hasPermission("group." + role.onGroup())) return Optional.of(new Status(role, Shift.ON));
        }
        for (Role role : roles.values()) {
            if (player.hasPermission("group." + role.offGroup())) return Optional.of(new Status(role, Shift.OFF));
        }
        return Optional.empty();
    }

    public boolean isOnShift(Player player) {
        return status(player).map(s -> s.shift() == Shift.ON).orElse(false);
    }

    /** Makes the player a staff member with this role, off shift. */
    public void give(String playerName, Role role) {
        console(setGroupCommand, playerName, role.offGroup());
    }

    public void take(String playerName) {
        console(removeStaffCommand, playerName, "default");
    }

    public void startShift(Player player, Role role) {
        console(setGroupCommand, player.getName(), role.onGroup());
        onStart.forEach(cmd -> console(cmd, player.getName(), role.onGroup()));
    }

    public void stopShift(Player player, Role role) {
        console(setGroupCommand, player.getName(), role.offGroup());
        onStop.forEach(cmd -> console(cmd, player.getName(), role.offGroup()));
    }

    public List<String> roleIds() {
        return new ArrayList<>(roles.keySet());
    }

    private void console(String template, String player, String group) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), template.replace("{player}", player).replace("{group}", group));
    }
}
