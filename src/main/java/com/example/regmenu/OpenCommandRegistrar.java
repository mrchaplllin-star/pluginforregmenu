package com.example.regmenu;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenCommandRegistrar {
  private final RegMenuPlugin plugin;
  private final Map<String, Command> registered = new HashMap<>();

  public OpenCommandRegistrar(RegMenuPlugin plugin) {
    this.plugin = plugin;
  }

  public void registerAll() {
    for (MenuData menu : plugin.getMenuManager().getMenus().values()) {
      for (String command : menu.getOpenCommands()) {
        registerCommand(menu.getName(), command);
      }
    }
  }

  public void registerCommand(String menuName, String rawCommand) {
    String label = normalize(rawCommand);
    if (label.isBlank()) {
      return;
    }
    CommandMap commandMap = getCommandMap();
    if (commandMap == null) {
      return;
    }
    Command existing = commandMap.getCommand(label);
    if (existing != null) {
      return;
    }
    BukkitCommand command = new BukkitCommand(label) {
      @Override
      public boolean execute(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
          sender.sendMessage("Only players can use this command.");
          return true;
        }
        MenuData menu = plugin.getMenuManager().getOrCreateMenu(menuName);
        MenuInventoryFactory.openMenu(player, menu);
        return true;
      }
    };
    commandMap.register(plugin.getName(), command);
    registered.put(label, command);
  }

  public void unregisterCommand(String rawCommand) {
    String label = normalize(rawCommand);
    Command command = registered.remove(label);
    if (command == null) {
      CommandMap commandMap = getCommandMap();
      if (commandMap != null) {
        command = commandMap.getCommand(label);
      }
    }
    if (command instanceof BukkitCommand bukkitCommand) {
      bukkitCommand.unregister(getCommandMap());
    }
  }

  public String normalize(String command) {
    if (command == null) {
      return "";
    }
    String trimmed = command.trim();
    if (trimmed.startsWith("/")) {
      trimmed = trimmed.substring(1);
    }
    return trimmed.toLowerCase(Locale.ROOT);
  }

  private CommandMap getCommandMap() {
    try {
      Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
      field.setAccessible(true);
      return (CommandMap) field.get(Bukkit.getServer());
    } catch (ReflectiveOperationException e) {
      plugin.getLogger().warning("Failed to access command map: " + e.getMessage());
      return null;
    }
  }
}
