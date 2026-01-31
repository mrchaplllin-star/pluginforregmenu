package com.example.regmenu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuItemData {
  private ItemStack itemStack;
  private String command;
  private CommandExecutorType executor;

  public MenuItemData(ItemStack itemStack) {
    this.itemStack = itemStack;
    this.executor = CommandExecutorType.PLAYER;
  }

  public ItemStack getItemStack() {
    return itemStack;
  }

  public void setItemStack(ItemStack itemStack) {
    this.itemStack = itemStack;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public CommandExecutorType getExecutor() {
    return executor;
  }

  public void setExecutor(CommandExecutorType executor) {
    this.executor = executor;
  }

  public void applyName(String name) {
    if (itemStack == null || itemStack.getType() == Material.AIR) {
      return;
    }
    ItemMeta meta = itemStack.getItemMeta();
    if (meta != null) {
      meta.setDisplayName(name);
      itemStack.setItemMeta(meta);
    }
  }

  public void save(ConfigurationSection section) {
    section.set("item", itemStack);
    section.set("command", command);
    section.set("executor", executor != null ? executor.name() : CommandExecutorType.PLAYER.name());
  }

  public static MenuItemData fromConfig(ConfigurationSection section) {
    ItemStack item = section.getItemStack("item");
    if (item == null) {
      return null;
    }
    MenuItemData data = new MenuItemData(item);
    data.setCommand(section.getString("command"));
    String executorName = section.getString("executor", CommandExecutorType.PLAYER.name());
    data.setExecutor(CommandExecutorType.fromString(executorName));
    return data;
  }
}
