package com.example.regmenu;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class MenuData {
  private final String name;
  private final int size;
  private final MenuInventoryType inventoryType;
  private final String typeId;
  private final List<String> openCommands = new ArrayList<>();
  private final Map<Integer, MenuItemData> items = new HashMap<>();

  public MenuData(String name, int size, MenuInventoryType inventoryType, String typeId) {
    this.name = name;
    this.size = size;
    this.inventoryType = inventoryType;
    this.typeId = typeId;
  }

  public String getName() {
    return name;
  }

  public int getSize() {
    return size;
  }

  public MenuInventoryType getInventoryType() {
    return inventoryType;
  }

  public String getTypeId() {
    return typeId;
  }

  public List<String> getOpenCommands() {
    return openCommands;
  }

  public Map<Integer, MenuItemData> getItems() {
    return items;
  }

  public void setItem(int slot, MenuItemData data) {
    if (data == null) {
      items.remove(slot);
    } else {
      items.put(slot, data);
    }
  }

  public MenuItemData getItem(int slot) {
    return items.get(slot);
  }

  public void save(YamlConfiguration config) {
    config.set("size", size);
    config.set("type", typeId);
    config.set("open-commands", openCommands);
    ConfigurationSection itemsSection = config.createSection("items");
    for (Map.Entry<Integer, MenuItemData> entry : items.entrySet()) {
      ConfigurationSection slotSection = itemsSection.createSection(String.valueOf(entry.getKey()));
      entry.getValue().save(slotSection);
    }
  }

  public static MenuData fromConfig(String name, YamlConfiguration config) {
    String typeId = config.getString("type", "chest");
    MenuInventoryType type = MenuInventoryType.fromId(typeId);
    int size = config.getInt("size", type.getSize());
    MenuData data = new MenuData(name, size, type, typeId);
    data.getOpenCommands().addAll(config.getStringList("open-commands"));
    ConfigurationSection itemsSection = config.getConfigurationSection("items");
    if (itemsSection != null) {
      for (String key : itemsSection.getKeys(false)) {
        try {
          int slot = Integer.parseInt(key);
          ConfigurationSection slotSection = itemsSection.getConfigurationSection(key);
          if (slotSection != null) {
            MenuItemData item = MenuItemData.fromConfig(slotSection);
            if (item != null) {
              data.setItem(slot, item);
            }
          }
        } catch (NumberFormatException ignored) {
          // skip invalid keys
        }
      }
    }
    return data;
  }
}
