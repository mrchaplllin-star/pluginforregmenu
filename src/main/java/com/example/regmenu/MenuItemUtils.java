package com.example.regmenu;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class MenuItemUtils {
  private static final PersistentDataType<Byte, Byte> DATA_TYPE = PersistentDataType.BYTE;

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

  private static NamespacedKey getFillerKey(RegMenuPlugin plugin) {
    return plugin.getFillerKey();
  }

  private static NamespacedKey getLockedKey(RegMenuPlugin plugin) {
    return plugin.getLockedKey();
  }
}
