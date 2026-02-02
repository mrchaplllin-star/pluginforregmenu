package com.example.regmenu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

public class MenuHolder implements InventoryHolder {
  public enum Mode {
    EDIT,
    VIEW
  }

  private final String menuName;
  private final Mode mode;

  public MenuHolder(String menuName, Mode mode) {
    this.menuName = menuName;
    this.mode = mode;
  }

  public String getMenuName() {
    return menuName;
  }

  public Mode getMode() {
    return mode;
  }

  @Override
  public @Nullable Inventory getInventory() {
    return null;
  }
}
