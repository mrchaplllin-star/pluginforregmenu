package com.example.regmenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegMenuCommand implements CommandExecutor, TabCompleter {
  private final RegMenuPlugin plugin;

  public RegMenuCommand(RegMenuPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                           @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(ChatColor.RED + "Only players can use this command.");
      return true;
    }
    if (!player.hasPermission("regmenu.edit")) {
      player.sendMessage(ChatColor.RED + "You do not have permission to edit menus.");
      return true;
    }
    if (args.length < 2) {
      sendUsage(player);
      return true;
    }

    String sub = args[0].toLowerCase();
    String menuName = args[1];
    MenuManager menuManager = plugin.getMenuManager();
    MenuData menu = menuManager.getOrCreateMenu(menuName);

    switch (sub) {
      case "decor" -> openEditor(player, menu);
      case "name" -> handleRename(player, menu, args);
      case "command" -> handleCommandAssign(player, menu, args);
      default -> sendUsage(player);
    }
    return true;
  }

  private void openEditor(Player player, MenuData menu) {
    Inventory inventory = plugin.getServer().createInventory(new MenuHolder(menu.getName(), MenuHolder.Mode.EDIT),
        menu.getSize(), ChatColor.DARK_GREEN + "Edit: " + menu.getName());
    menu.getItems().forEach((slot, item) -> inventory.setItem(slot, item.getItemStack()));
    plugin.getEditorSessions().put(player.getUniqueId(), new EditorSession(menu.getName(), inventory));
    player.openInventory(inventory);
    player.sendMessage(ChatColor.GREEN + "Editing menu: " + menu.getName());
  }

  private void handleRename(Player player, MenuData menu, String[] args) {
    if (args.length < 3) {
      player.sendMessage(ChatColor.RED + "Usage: /regmenu name <menu> <slot>" );
      return;
    }
    int slot = parseSlot(player, args[2], menu.getSize());
    if (slot < 0) {
      return;
    }
    MenuItemData itemData = menu.getItem(slot);
    if (itemData == null || itemData.getItemStack() == null || itemData.getItemStack().getType() == Material.AIR) {
      player.sendMessage(ChatColor.RED + "That slot is empty in this menu.");
      return;
    }
    openAnvilInput(player, new PendingInput(PendingInput.Type.NAME, menu.getName(), slot),
        ChatColor.YELLOW + "Enter new name");
  }

  private void handleCommandAssign(Player player, MenuData menu, String[] args) {
    if (args.length < 3) {
      player.sendMessage(ChatColor.RED + "Usage: /regmenu command <menu> <slot> [anvil|chat]");
      return;
    }
    int slot = parseSlot(player, args[2], menu.getSize());
    if (slot < 0) {
      return;
    }
    MenuItemData itemData = menu.getItem(slot);
    if (itemData == null || itemData.getItemStack() == null || itemData.getItemStack().getType() == Material.AIR) {
      player.sendMessage(ChatColor.RED + "That slot is empty in this menu.");
      return;
    }
    String mode = args.length >= 4 ? args[3].toLowerCase() : "anvil";
    PendingInput pending = new PendingInput(PendingInput.Type.COMMAND, menu.getName(), slot);
    if (mode.equals("chat")) {
      plugin.getPendingChatInputs().put(player.getUniqueId(), pending);
      player.sendMessage(ChatColor.GOLD + "Type the command in chat. Use 'console:' or 'player:' prefix to choose executor.");
      player.sendMessage(ChatColor.GRAY + "Example: console:/say Hello or /warp spawn");
    } else {
      openAnvilInput(player, pending, ChatColor.YELLOW + "Enter command");
    }
  }

  private void openAnvilInput(Player player, PendingInput pendingInput, String title) {
    Inventory inventory = plugin.getServer().createInventory(player, org.bukkit.event.inventory.InventoryType.ANVIL,
        title);
    ItemStack paper = new ItemStack(Material.PAPER);
    ItemMeta meta = paper.getItemMeta();
    if (meta != null) {
      meta.setDisplayName(" ");
      paper.setItemMeta(meta);
    }
    inventory.setItem(0, paper);
    plugin.getPendingAnvilInputs().put(player.getUniqueId(), pendingInput);
    InventoryView view = player.openInventory(inventory);
    if (view.getTopInventory() instanceof AnvilInventory anvilInventory) {
      anvilInventory.setItem(0, paper);
    }
  }

  private int parseSlot(Player player, String slotArg, int size) {
    try {
      int slot = Integer.parseInt(slotArg);
      if (slot < 1 || slot > size) {
        player.sendMessage(ChatColor.RED + "Slot must be between 1 and " + size + ".");
        return -1;
      }
      return slot - 1;
    } catch (NumberFormatException ex) {
      player.sendMessage(ChatColor.RED + "Slot must be a number.");
      return -1;
    }
  }

  private void sendUsage(Player player) {
    player.sendMessage(ChatColor.YELLOW + "Usage:");
    player.sendMessage(ChatColor.GRAY + "/regmenu decor <menu>");
    player.sendMessage(ChatColor.GRAY + "/regmenu name <menu> <slot>");
    player.sendMessage(ChatColor.GRAY + "/regmenu command <menu> <slot> [anvil|chat]");
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                              @NotNull String alias, @NotNull String[] args) {
    if (args.length == 1) {
      return Arrays.asList("decor", "name", "command");
    }
    if (args.length == 4 && args[0].equalsIgnoreCase("command")) {
      return Arrays.asList("anvil", "chat");
    }
    return new ArrayList<>();
  }
}
