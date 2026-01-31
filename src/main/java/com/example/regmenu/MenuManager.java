package com.example.regmenu;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;

public class MenuManager {
  private final RegMenuPlugin plugin;
  private final File menusFolder;
  private final Map<String, MenuData> menus = new HashMap<>();

  public MenuManager(RegMenuPlugin plugin) {
    this.plugin = plugin;
    menusFolder = new File(plugin.getDataFolder(), "menus");
    if (!menusFolder.exists()) {
      menusFolder.mkdirs();
    }
  }

  public MenuData getOrCreateMenu(String name) {
    String key = normalizeName(name);
    return menus.computeIfAbsent(key, this::loadMenu);
  }

  public MenuData loadMenu(String normalizedName) {
    File file = new File(menusFolder, normalizedName + ".yml");
    if (!file.exists()) {
      return new MenuData(normalizedName, 54);
    }
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
    return MenuData.fromConfig(normalizedName, config);
  }

  public void saveMenu(MenuData menu) {
    File file = new File(menusFolder, menu.getName() + ".yml");
    YamlConfiguration config = new YamlConfiguration();
    menu.save(config);
    try {
      config.save(file);
    } catch (IOException e) {
      plugin.getLogger().warning("Failed to save menu " + menu.getName() + ": " + e.getMessage());
    }
  }

  public String normalizeName(String name) {
    return name.toLowerCase(Locale.ROOT);
  }

  public File getMenusFolder() {
    return menusFolder;
  }
}
