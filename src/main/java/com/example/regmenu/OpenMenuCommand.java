package com.example.regmenu;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenMenuCommand implements CommandExecutor, TabCompleter {
  private final RegMenuPlugin plugin;

  public OpenMenuCommand(RegMenuPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                           @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(ChatColor.RED + "Only players can use this command.");
      return true;
    }
    if (!player.hasPermission("regmenu.open")) {
      player.sendMessage(ChatColor.RED + "You do not have permission to open menus.");
      return true;
    }
    if (args.length < 1) {
      player.sendMessage(ChatColor.RED + "Usage: /openmenu <menu>");
      return true;
    }
    String name = args[0];
    MenuData menu = plugin.getMenuManager().getOrCreateMenu(name);
    MenuInventoryFactory.openMenu(plugin, player, menu);
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                              @NotNull String alias, @NotNull String[] args) {
    return new ArrayList<>();
  }
}
