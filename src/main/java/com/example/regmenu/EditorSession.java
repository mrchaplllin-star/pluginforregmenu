package com.example.regmenu;

import org.bukkit.inventory.Inventory;

public class EditorSession {
  private final String menuName;
  private final Inventory inventory;

  public EditorSession(String menuName, Inventory inventory) {
    this.menuName = menuName;
    this.inventory = inventory;
  }

  public String getMenuName() {
    return menuName;
  }

  public Inventory getInventory() {
    return inventory;
  }
}
