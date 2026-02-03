package com.example.regmenu;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class MenuItemUtils {
  private static final PersistentDataType<Byte, Byte> DATA_TYPE = PersistentDataType.BYTE;
  private static final PersistentDataType<String, String> STRING_DATA_TYPE = PersistentDataType.STRING;

  private MenuItemUtils() {
  }

  public static ItemStack createFiller(RegMenuPlugin plugin) {
    ItemStack stack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    ItemMeta meta = stack.getItemMeta();
    if (meta != null) {
      meta.setDisplayName(" ");
      PersistentDataContainer container = meta.getPersistentDataContainer();
      container.set(getFillerKey(plugin), DATA_TYPE, (byte) 1);
      stack.setItemMeta(meta);
    }
    return stack;
  }

  public static boolean isFiller(RegMenuPlugin plugin, ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      return false;
    }
    ItemMeta meta = stack.getItemMeta();
    if (meta == null) {
      return false;
    }
    PersistentDataContainer container = meta.getPersistentDataContainer();
    return container.has(getFillerKey(plugin), DATA_TYPE);
  }

  public static void setLocked(RegMenuPlugin plugin, ItemStack stack, boolean locked) {
    if (stack == null || stack.getType() == Material.AIR) {
      return;
    }
    ItemMeta meta = stack.getItemMeta();
    if (meta == null) {
      return;
    }
    PersistentDataContainer container = meta.getPersistentDataContainer();
    if (locked) {
      container.set(getLockedKey(plugin), DATA_TYPE, (byte) 1);
    } else {
      container.remove(getLockedKey(plugin));
    }
    stack.setItemMeta(meta);
  }

  public static boolean isLocked(RegMenuPlugin plugin, ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      return false;
    }
    ItemMeta meta = stack.getItemMeta();
    if (meta == null) {
      return false;
    }
    PersistentDataContainer container = meta.getPersistentDataContainer();
    return container.has(getLockedKey(plugin), DATA_TYPE);
  }

  public static void setLinkedMenu(RegMenuPlugin plugin, ItemStack stack, String menuName) {
    if (stack == null || stack.getType() == Material.AIR) {
      return;
    }
    ItemMeta meta = stack.getItemMeta();
    if (meta == null) {
      return;
    }
    PersistentDataContainer container = meta.getPersistentDataContainer();
    if (menuName == null || menuName.isBlank()) {
      container.remove(getLinkedMenuKey(plugin));
    } else {
      container.set(getLinkedMenuKey(plugin), STRING_DATA_TYPE, menuName);
    }
    stack.setItemMeta(meta);
  }

  public static void setLinkedMenuMode(RegMenuPlugin plugin, ItemStack stack, LinkedMenuMode mode) {
    if (stack == null || stack.getType() == Material.AIR) {
      return;
    }
    ItemMeta meta = stack.getItemMeta();
    if (meta == null) {
      return;
    }
    PersistentDataContainer container = meta.getPersistentDataContainer();
    LinkedMenuMode resolved = mode == null ? LinkedMenuMode.INVENT : mode;
    container.set(getLinkedMenuModeKey(plugin), STRING_DATA_TYPE, resolved.name());
    stack.setItemMeta(meta);
  }

  public static String getLinkedMenu(RegMenuPlugin plugin, ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      return null;
    }
    ItemMeta meta = stack.getItemMeta();
    if (meta == null) {
      return null;
    }
    PersistentDataContainer container = meta.getPersistentDataContainer();
    return container.get(getLinkedMenuKey(plugin), STRING_DATA_TYPE);
  }

  public static LinkedMenuMode getLinkedMenuMode(RegMenuPlugin plugin, ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      return LinkedMenuMode.INVENT;
    }
    ItemMeta meta = stack.getItemMeta();
    if (meta == null) {
      return LinkedMenuMode.INVENT;
    }
    PersistentDataContainer container = meta.getPersistentDataContainer();
    String value = container.get(getLinkedMenuModeKey(plugin), STRING_DATA_TYPE);
    return LinkedMenuMode.fromString(value);
  }

  private static NamespacedKey getFillerKey(RegMenuPlugin plugin) {
    return plugin.getFillerKey();
  }

  private static NamespacedKey getLockedKey(RegMenuPlugin plugin) {
    return plugin.getLockedKey();
  }

  private static NamespacedKey getLinkedMenuKey(RegMenuPlugin plugin) {
    return plugin.getLinkedMenuKey();
  }

  private static NamespacedKey getLinkedMenuModeKey(RegMenuPlugin plugin) {
    return plugin.getLinkedMenuModeKey();
  }
}
