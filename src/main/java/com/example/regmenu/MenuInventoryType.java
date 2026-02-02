package com.example.regmenu;

import java.util.Locale;
import org.bukkit.event.inventory.InventoryType;

public enum MenuInventoryType {
  CHEST("chest", 27, null),
  LARGE_CHEST("large_chest", 54, null),
  ENDER_CHEST("ender_chest", 27, InventoryType.ENDER_CHEST),
  BARREL("barrel", 27, InventoryType.BARREL),
  SHULKER_BOX("shulker", 27, InventoryType.SHULKER_BOX);

  private final String id;
  private final int size;
  private final InventoryType inventoryType;

  MenuInventoryType(String id, int size, InventoryType inventoryType) {
    this.id = id;
    this.size = size;
    this.inventoryType = inventoryType;
  }

  public String getId() {
    return id;
  }

  public int getSize() {
    return size;
  }

  public InventoryType getInventoryType() {
    return inventoryType;
  }

  public static MenuInventoryType fromId(String id) {
    if (id == null) {
      return CHEST;
    }
    String normalized = id.toLowerCase(Locale.ROOT);
    if (normalized.startsWith("shulker")) {
      return SHULKER_BOX;
    }
    for (MenuInventoryType type : values()) {
      if (type.id.equals(normalized)) {
        return type;
      }
    }
    return CHEST;
  }
}
