package com.example.regmenu;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class MenuData {
  private final String name;
  private final int size;
  private final Map<Integer, MenuItemData> items = new HashMap<>();

  public MenuData(String name, int size) {
    this.name = name;
    this.size = size;
  }

  public String getName() {
    return name;
  }

  public int getSize() {
    return size;
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
    ConfigurationSection itemsSection = config.createSection("items");
    for (Map.Entry<Integer, MenuItemData> entry : items.entrySet()) {
      ConfigurationSection slotSection = itemsSection.createSection(String.valueOf(entry.getKey()));
      entry.getValue().save(slotSection);
    }
  }

  public static MenuData fromConfig(String name, YamlConfiguration config) {
    int size = config.getInt("size", 54);
    MenuData data = new MenuData(name, size);
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
