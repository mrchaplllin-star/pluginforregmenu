package com.example.regmenu;

import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

    if (event.getInventory() instanceof AnvilInventory && plugin.getPendingAnvilInputs().containsKey(player.getUniqueId())) {
      if (event.getRawSlot() == 2 && event.getClick() != ClickType.NUMBER_KEY) {
        event.setCancelled(true);
        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType() == Material.AIR) {
          return;
        }
        PendingInput pending = plugin.getPendingAnvilInputs().remove(player.getUniqueId());
        String input = getItemName(result);
        if (input == null || input.isBlank()) {
          player.sendMessage(ChatColor.RED + "Input cannot be empty.");
          player.closeInventory();
          return;
        }
        applyInput(player, pending, input.trim());
        player.closeInventory();
      }
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

    if (event.getInventory() instanceof AnvilInventory) {
      plugin.getPendingAnvilInputs().remove(player.getUniqueId());
    }
  }

  @EventHandler
  public void onChatInput(AsyncPlayerChatEvent event) {
    Player player = event.getPlayer();
    PendingInput pending = plugin.getPendingChatInputs().remove(player.getUniqueId());
    if (pending == null) {
      return;
    }
    event.setCancelled(true);
    String message = event.getMessage();
    Bukkit.getScheduler().runTask(plugin, () -> applyInput(player, pending, message));
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

  private void applyInput(Player player, PendingInput pending, String input) {
    MenuData menu = plugin.getMenuManager().getOrCreateMenu(pending.getMenuName());
    MenuItemData item = menu.getItem(pending.getSlot());
    if (item == null || item.getItemStack() == null || item.getItemStack().getType() == Material.AIR) {
      player.sendMessage(ChatColor.RED + "That slot is empty in this menu.");
      return;
    }
    if (pending.getType() == PendingInput.Type.NAME) {
      item.applyName(input);
      menu.setItem(pending.getSlot(), item);
      plugin.getMenuManager().saveMenu(menu);
      player.sendMessage(ChatColor.GREEN + "Name updated.");
      return;
    }
    String commandInput = input.trim();
    CommandExecutorType executor = CommandExecutorType.PLAYER;
    String normalized = commandInput.toLowerCase(Locale.ROOT);
    if (normalized.startsWith("console:")) {
      executor = CommandExecutorType.CONSOLE;
      commandInput = commandInput.substring("console:".length()).trim();
    } else if (normalized.startsWith("player:")) {
      executor = CommandExecutorType.PLAYER;
      commandInput = commandInput.substring("player:".length()).trim();
    }
    item.setCommand(commandInput);
    item.setExecutor(executor);
    menu.setItem(pending.getSlot(), item);
    plugin.getMenuManager().saveMenu(menu);
    player.sendMessage(ChatColor.GREEN + "Command assigned.");
  }

  private String getItemName(ItemStack item) {
    ItemMeta meta = item.getItemMeta();
    if (meta != null && meta.hasDisplayName()) {
      return meta.getDisplayName();
    }
    return item.getType().name();
  }
}
