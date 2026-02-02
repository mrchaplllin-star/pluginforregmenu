package com.example.regmenu;

import java.util.Locale;

public enum CommandExecutorType {
  PLAYER,
  CONSOLE;

  public static CommandExecutorType fromString(String value) {
    if (value == null) {
      return PLAYER;
    }
    try {
      return CommandExecutorType.valueOf(value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      return PLAYER;
    }
  }
}
