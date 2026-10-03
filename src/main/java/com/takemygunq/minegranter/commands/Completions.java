package com.takemygunq.minegranter.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

final class Completions {

    private Completions() {
    }

    static List<String> matching(String prefix, Collection<String> options) {
        String p = prefix.toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(p)).sorted().toList();
    }

    static List<String> onlinePlayers(String prefix) {
        return matching(prefix, Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
    }
}
