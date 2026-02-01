package com.example.regmenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
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
      case "create" -> handleCreate(player, menu, args);
      case "decor" -> openEditor(player, menu);
      case "name" -> handleRename(player, menu, args);
      case "command" -> handleCommandAssign(player, menu, args);
      case "opencommand" -> handleOpenCommand(player, menu, args);
      default -> sendUsage(player);
    }
    return true;
  }

  private void handleCreate(Player player, MenuData menu, String[] args) {
    if (args.length < 3) {
      player.sendMessage(ChatColor.RED + "Usage: /regmenu create <menu> <type>");
      return;
    }
    String typeId = args[2].toLowerCase();
    if (!isValidType(typeId)) {
      player.sendMessage(ChatColor.RED + "Invalid type. Use: chest, large_chest, ender_chest, barrel, shulker_color");
      return;
    }
    MenuInventoryType type = MenuInventoryType.fromId(typeId);
    MenuData updated = new MenuData(menu.getName(), type.getSize(), type, typeId);
    for (var entry : menu.getItems().entrySet()) {
      if (entry.getKey() >= 0 && entry.getKey() < updated.getSize()) {
        updated.setItem(entry.getKey(), entry.getValue());
      }
    }
    updated.getOpenCommands().addAll(menu.getOpenCommands());
    plugin.getMenuManager().getMenus().put(plugin.getMenuManager().normalizeName(menu.getName()), updated);
    plugin.getMenuManager().saveMenu(updated);
    player.sendMessage(ChatColor.GREEN + "Menu created: " + updated.getName() + " (" + typeId + ")");
  }

  private void openEditor(Player player, MenuData menu) {
    org.bukkit.inventory.Inventory inventory;
    if (menu.getInventoryType().getInventoryType() == null) {
      inventory = plugin.getServer().createInventory(new MenuHolder(menu.getName(), MenuHolder.Mode.EDIT),
          menu.getSize(), ChatColor.DARK_GREEN + "Edit: " + menu.getName());
    } else {
      inventory = plugin.getServer().createInventory(new MenuHolder(menu.getName(), MenuHolder.Mode.EDIT),
          menu.getInventoryType().getInventoryType(), ChatColor.DARK_GREEN + "Edit: " + menu.getName());
    }
    menu.getItems().forEach((slot, item) -> {
      if (slot >= 0 && slot < inventory.getSize()) {
        inventory.setItem(slot, item.getItemStack());
      }
    });
    plugin.getEditorSessions().put(player.getUniqueId(), new EditorSession(menu.getName(), inventory));
    player.openInventory(inventory);
    player.sendMessage(ChatColor.GREEN + "Editing menu: " + menu.getName());
  }

  private void handleRename(Player player, MenuData menu, String[] args) {
    if (args.length < 4) {
      player.sendMessage(ChatColor.RED + "Usage: /regmenu name <menu> <slot> <name>");
      return;
    }
    int slot = parseSlot(player, args[2], menu.getSize());
    if (slot < 0) {
      return;
    }
    MenuItemData itemData = menu.getItem(slot);
    if (itemData == null || itemData.getItemStack() == null) {
      player.sendMessage(ChatColor.RED + "That slot is empty in this menu.");
      return;
    }
    String name = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
    itemData.applyName(ChatColor.translateAlternateColorCodes('&', name));
    menu.setItem(slot, itemData);
    plugin.getMenuManager().saveMenu(menu);
    player.sendMessage(ChatColor.GREEN + "Name updated.");
  }

  private void handleCommandAssign(Player player, MenuData menu, String[] args) {
    if (args.length < 5) {
      player.sendMessage(ChatColor.RED + "Usage: /regmenu command <menu> <slot> <player|console> <command>");
      return;
    }
    int slot = parseSlot(player, args[2], menu.getSize());
    if (slot < 0) {
      return;
    }
    MenuItemData itemData = menu.getItem(slot);
    if (itemData == null || itemData.getItemStack() == null) {
      player.sendMessage(ChatColor.RED + "That slot is empty in this menu.");
      return;
    }
    CommandExecutorType executor = CommandExecutorType.fromString(args[3]);
    String command = String.join(" ", java.util.Arrays.copyOfRange(args, 4, args.length));
    itemData.setCommand(command);
    itemData.setExecutor(executor);
    menu.setItem(slot, itemData);
    plugin.getMenuManager().saveMenu(menu);
    player.sendMessage(ChatColor.GREEN + "Command assigned.");
  }

  private void handleOpenCommand(Player player, MenuData menu, String[] args) {
    if (args.length < 4) {
      player.sendMessage(ChatColor.RED + "Usage: /regmenu opencommand <add|remove> <menu> <command>");
      return;
    }
    String action = args[2].toLowerCase();
    String command = args[3];
    String normalized = plugin.getOpenCommandRegistrar().normalize(command);
    if (normalized.isBlank()) {
      player.sendMessage(ChatColor.RED + "Command cannot be empty.");
      return;
    }
    if (action.equals("add")) {
      if (menu.getOpenCommands().contains(normalized)) {
        player.sendMessage(ChatColor.RED + "That open command already exists.");
        return;
      }
      menu.getOpenCommands().add(normalized);
      plugin.getMenuManager().saveMenu(menu);
      plugin.getOpenCommandRegistrar().registerCommand(menu.getName(), normalized);
      player.sendMessage(ChatColor.GREEN + "Open command added: /" + normalized);
      return;
    }
    if (action.equals("remove")) {
      if (!menu.getOpenCommands().remove(normalized)) {
        player.sendMessage(ChatColor.RED + "That open command does not exist.");
        return;
      }
      plugin.getMenuManager().saveMenu(menu);
      plugin.getOpenCommandRegistrar().unregisterCommand(normalized);
      player.sendMessage(ChatColor.GREEN + "Open command removed: /" + normalized);
      return;
    }
    player.sendMessage(ChatColor.RED + "Usage: /regmenu opencommand <add|remove> <menu> <command>");
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
    player.sendMessage(ChatColor.GRAY + "/regmenu create <menu> <type>");
    player.sendMessage(ChatColor.GRAY + "/regmenu decor <menu>");
    player.sendMessage(ChatColor.GRAY + "/regmenu name <menu> <slot> <name>");
    player.sendMessage(ChatColor.GRAY + "/regmenu command <menu> <slot> <player|console> <command>");
    player.sendMessage(ChatColor.GRAY + "/regmenu opencommand <add|remove> <menu> <command>");
  }

  private boolean isValidType(String typeId) {
    if (typeId == null) {
      return false;
    }
    if (typeId.startsWith("shulker_")) {
      return true;
    }
    return typeId.equals("chest")
        || typeId.equals("large_chest")
        || typeId.equals("ender_chest")
        || typeId.equals("barrel");
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                              @NotNull String alias, @NotNull String[] args) {
    if (args.length == 1) {
      return Arrays.asList("create", "decor", "name", "command", "opencommand");
    }
    if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
      return Arrays.asList("chest", "large_chest", "ender_chest", "barrel", "shulker_red");
    }
    if (args.length == 3 && args[0].equalsIgnoreCase("opencommand")) {
      return Arrays.asList("add", "remove");
    }
    if (args.length == 4 && args[0].equalsIgnoreCase("command")) {
      return Arrays.asList("player", "console");
    }
    return new ArrayList<>();
  }
}
