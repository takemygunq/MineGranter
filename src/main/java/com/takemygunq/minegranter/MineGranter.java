package com.takemygunq.minegranter;

import com.takemygunq.minegranter.commands.ReportCommand;
import com.takemygunq.minegranter.commands.ShiftCommand;
import com.takemygunq.minegranter.commands.StaffCommand;
import com.takemygunq.minegranter.listeners.ShiftListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class MineGranter extends JavaPlugin {

    private Messages messages;
    private StaffService staff;
    private TelegramNotifier telegram;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages = new Messages(this);
        telegram = new TelegramNotifier(this);
        staff = new StaffService(this);
        reload();

        register("staff", new StaffCommand(this));
        register("sw", new ShiftCommand(this));
        register("report", new ReportCommand(this));
        getServer().getPluginManager().registerEvents(new ShiftListener(this), this);
    }

    /** Re-reads config.yml and everything built from it. */
    public void reload() {
        reloadConfig();
        messages.load(getConfig());
        staff.load(getConfig());
        telegram.load(getConfig());
    }

    private void register(String name, TabExecutor executor) {
        PluginCommand command = Objects.requireNonNull(getCommand(name), "Command missing from plugin.yml: " + name);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    public Messages messages() {
        return messages;
    }

    public StaffService staff() {
        return staff;
    }

    public TelegramNotifier telegram() {
        return telegram;
    }
}
