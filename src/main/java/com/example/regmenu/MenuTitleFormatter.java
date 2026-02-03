package com.example.regmenu;

import org.bukkit.ChatColor;

public final class MenuTitleFormatter {
  private static final int MAX_TITLE_LENGTH = 32;

  private MenuTitleFormatter() {
  }

  public static String format(MenuData menu) {
    String baseTitle = menu.getTitle() == null ? menu.getName() : menu.getTitle();
    String colored = ChatColor.translateAlternateColorCodes('&', baseTitle);
    String stripped = ChatColor.stripColor(colored);
    if (stripped == null) {
      stripped = "";
    }
    int visibleLength = Math.min(stripped.length(), MAX_TITLE_LENGTH);
    if (menu.getTitleAlignment() == MenuTitleAlignment.LEFT) {
      return colored;
    }
    int padding;
    if (menu.getTitleAlignment() == MenuTitleAlignment.CENTER) {
      padding = Math.max(0, (MAX_TITLE_LENGTH - visibleLength) / 2);
    } else {
      padding = Math.max(0, MAX_TITLE_LENGTH - visibleLength);
    }
    return " ".repeat(padding) + colored;
  }
}
