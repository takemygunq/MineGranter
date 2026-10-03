package com.takemygunq.minegranter.listeners;

import com.takemygunq.minegranter.MineGranter;
import com.takemygunq.minegranter.StaffService.Shift;
import com.takemygunq.minegranter.TelegramNotifier.Chat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.EnumSet;
import java.util.Set;

/** What staff members can't do while on shift, and ending the shift when they leave. */
public final class ShiftListener implements Listener {

    private static final Set<InventoryType> CONTAINERS = EnumSet.of(
            InventoryType.CHEST, InventoryType.ENDER_CHEST, InventoryType.BARREL, InventoryType.SHULKER_BOX,
            InventoryType.HOPPER, InventoryType.DISPENSER, InventoryType.DROPPER);

    private final MineGranter plugin;

    public ShiftListener(MineGranter plugin) {
        this.plugin = plugin;
    }

    private boolean restricted(Player player, String rule) {
        return plugin.getConfig().getBoolean("restrictions." + rule, true)
                && !player.hasPermission("minegranter.bypass")
                && plugin.staff().isOnShift(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player attacker = null;
        if (event.getDamager() instanceof Player p) attacker = p;
        else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) attacker = p;
        if (attacker != null && restricted(attacker, "attack_players")) {
            event.setCancelled(true);
            plugin.messages().send(attacker, "restricted");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && restricted(player, "pickup_items")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player
                && CONTAINERS.contains(event.getInventory().getType())
                && restricted(player, "open_containers")) {
            event.setCancelled(true);
            plugin.messages().send(player, "restricted");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!plugin.getConfig().getBoolean("restrictions.end_shift_on_quit", true)) return;
        Player player = event.getPlayer();
        plugin.staff().status(player)
                .filter(s -> s.shift() == Shift.ON)
                .ifPresent(s -> {
                    plugin.staff().stopShift(player, s.role());
                    plugin.telegram().notify(Chat.STAFF, "shift_stopped", "player", player.getName(), "role", s.role().prefix());
                });
    }
}
