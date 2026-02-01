package com.example.regmenu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MenuListener implements Listener {
  private final RegMenuPlugin plugin;

  public MenuListener(RegMenuPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }

    if (event.getInventory().getHolder() instanceof MenuHolder holder) {
      if (holder.getMode() == MenuHolder.Mode.VIEW) {
        event.setCancelled(true);
        if (event.getRawSlot() >= 0 && event.getRawSlot() < event.getInventory().getSize()) {
          handleMenuClick(player, holder.getMenuName(), event.getRawSlot());
        }
      }
      return;
    }

  }

  @EventHandler
  public void onInventoryDrag(InventoryDragEvent event) {
    if (event.getInventory().getHolder() instanceof MenuHolder holder
        && holder.getMode() == MenuHolder.Mode.VIEW) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player player)) {
      return;
    }
    if (event.getInventory().getHolder() instanceof MenuHolder holder
        && holder.getMode() == MenuHolder.Mode.EDIT) {
      String menuName = holder.getMenuName();
      EditorSession session = plugin.getEditorSessions().remove(player.getUniqueId());
      if (session != null && session.getMenuName().equalsIgnoreCase(menuName)) {
        saveInventoryToMenu(menuName, session.getInventory());
        player.sendMessage(ChatColor.GREEN + "Menu saved: " + menuName);
      }
    }

  }

  private void saveInventoryToMenu(String menuName, Inventory inventory) {
    MenuData menu = plugin.getMenuManager().getOrCreateMenu(menuName);
    for (int slot = 0; slot < menu.getSize(); slot++) {
      ItemStack item = inventory.getItem(slot);
      if (item == null || item.getType() == Material.AIR) {
        menu.setItem(slot, null);
      } else {
        MenuItemData data = menu.getItem(slot);
        if (data == null) {
          data = new MenuItemData(item.clone());
        } else {
          data.setItemStack(item.clone());
        }
        menu.setItem(slot, data);
      }
    }
    plugin.getMenuManager().saveMenu(menu);
  }

  private void handleMenuClick(Player player, String menuName, int slot) {
    MenuData menu = plugin.getMenuManager().getOrCreateMenu(menuName);
    MenuItemData item = menu.getItem(slot);
    if (item == null) {
      return;
    }
    String command = item.getCommand();
    if (command == null || command.isBlank()) {
      return;
    }
    CommandExecutorType executor = item.getExecutor();
    String sanitized = command.startsWith("/") ? command.substring(1) : command;
    CommandSender sender = executor == CommandExecutorType.CONSOLE
        ? Bukkit.getConsoleSender()
        : player;
    Bukkit.dispatchCommand(sender, sanitized);
  }

}
