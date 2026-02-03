package com.example.regmenu;

public enum LinkedMenuMode {
  HOTBAR,
  INVENT;

  public static LinkedMenuMode fromString(String value) {
    if (value == null) {
      return INVENT;
    }
    return switch (value.toLowerCase()) {
      case "hodbar" -> HOTBAR;
      case "hotbar" -> HOTBAR;
      case "invent" -> INVENT;
      default -> INVENT;
    };
  }
}
