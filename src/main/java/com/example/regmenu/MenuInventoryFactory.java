package com.example.regmenu;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class MenuInventoryFactory {
  private MenuInventoryFactory() {
  }

  public static void openMenu(Player player, MenuData menu) {
    Inventory inventory = player.getServer().createInventory(new MenuHolder(menu.getName(), MenuHolder.Mode.VIEW),
        menu.getSize(), ChatColor.DARK_AQUA + "Menu: " + menu.getName());
    menu.getItems().forEach((slot, item) -> inventory.setItem(slot, item.getItemStack()));
    player.openInventory(inventory);
  }
}
