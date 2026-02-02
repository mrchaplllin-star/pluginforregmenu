package com.example.regmenu;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class MenuInventoryFactory {
  private MenuInventoryFactory() {
  }

  public static void openMenu(RegMenuPlugin plugin, Player player, MenuData menu) {
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
    fillEmptySlots(plugin, inventory);
    player.openInventory(inventory);
  }

  private static void fillEmptySlots(RegMenuPlugin plugin, Inventory inventory) {
    for (int slot = 0; slot < inventory.getSize(); slot++) {
      if (inventory.getItem(slot) == null || inventory.getItem(slot).getType() == org.bukkit.Material.AIR) {
        inventory.setItem(slot, MenuItemUtils.createFiller(plugin));
      }
    }
  }
}
