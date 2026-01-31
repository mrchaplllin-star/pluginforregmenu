package com.example.regmenu;

public class PendingInput {
  public enum Type {
    NAME,
    COMMAND
  }

  private final Type type;
  private final String menuName;
  private final int slot;

  public PendingInput(Type type, String menuName, int slot) {
    this.type = type;
    this.menuName = menuName;
    this.slot = slot;
  }

  public Type getType() {
    return type;
  }

  public String getMenuName() {
    return menuName;
  }

  public int getSlot() {
    return slot;
  }
}
