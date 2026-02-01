package com.example.regmenu;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class MenuInventoryFactory {
  private MenuInventoryFactory() {
  }

  public static void openMenu(Player player, MenuData menu) {
    Inventory inventory;
    String title = MenuTitleFormatter.format(menu);
    if (menu.getInventoryType().getInventoryType() == null) {
      inventory = player.getServer().createInventory(new MenuHolder(menu.getName(), MenuHolder.Mode.VIEW),
          menu.getSize(), title);
    } else {
      inventory = player.getServer().createInventory(new MenuHolder(menu.getName(), MenuHolder.Mode.VIEW),
          menu.getInventoryType().getInventoryType(), title);
    }
    menu.getItems().forEach((slot, item) -> {
      if (slot >= 0 && slot < inventory.getSize()) {
        inventory.setItem(slot, item.getItemStack());
      }
    });
    player.openInventory(inventory);
  }
}
