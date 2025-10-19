package com.jolly.xPStore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.EquipmentSlot;

public final class XPStore extends JavaPlugin implements Listener {

    private static final String PERMISSION_USE = "xpstore.use";
    private static final int EXPERIENCE_COST = 30;
    private static final Component NO_PERMISSION_MESSAGE = Component.text("You don't have permission to use XP Bottling!").color(NamedTextColor.RED);
    private static final Component NOT_ENOUGH_XP_MESSAGE = Component.text("You don't have enough XP to store it in a bottle!").color(NamedTextColor.RED);
    private static final float SOUND_VOLUME = 1.0f;
    private static final float SOUND_PITCH = 1.25f;

    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("XPStore has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("XPStore has been disabled!");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();
        ItemStack clickedItem = player.getInventory().getItemInMainHand();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;
        if (clickedBlock == null || clickedBlock.getType() != Material.ENCHANTING_TABLE) return;
        if (player.getInventory().getItemInMainHand().getType() != Material.GLASS_BOTTLE) return;
        if (clickedItem.getType() == Material.EXPERIENCE_BOTTLE) return;
        event.setCancelled(true);
        if (!player.hasPermission(PERMISSION_USE)) {
            player.sendMessage(NO_PERMISSION_MESSAGE);
            return;
        }
        if (player.getTotalExperience() < EXPERIENCE_COST && player.getLevel() < 1) {
            player.sendMessage(NOT_ENOUGH_XP_MESSAGE);
            return;
        }
        processBottling(player);
    }

    private void processBottling(Player player) {
        if (player.isSneaking()) {
            int bottleAmount = player.getInventory().getItemInMainHand().getAmount();
            if (bottleAmount > 0) {
                player.giveExp(-(bottleAmount*EXPERIENCE_COST));
                player.getInventory().removeItem(new ItemStack(Material.GLASS_BOTTLE, bottleAmount));
                Bukkit.getRegionScheduler().runDelayed(this, player.getLocation(), (scheduledTask ->
                        player.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, bottleAmount))), 1L);
            }
        } else {
            player.giveExp(-EXPERIENCE_COST);
            player.getInventory().removeItem(new ItemStack(Material.GLASS_BOTTLE, 1));
            Bukkit.getRegionScheduler().runDelayed(this, player.getLocation(), (scheduledTask ->
                    player.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, 1))), 1L);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, SOUND_VOLUME, SOUND_PITCH);
    }
}