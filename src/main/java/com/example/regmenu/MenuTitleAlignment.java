package com.example.regmenu;

import java.util.Locale;

public enum MenuTitleAlignment {
  LEFT,
  CENTER,
  RIGHT;

  public static MenuTitleAlignment fromString(String value) {
    if (value == null) {
      return LEFT;
    }
    try {
      return MenuTitleAlignment.valueOf(value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      return LEFT;
    }
  }
}
