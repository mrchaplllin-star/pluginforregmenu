package com.example.regmenu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class RegMenuPlugin extends JavaPlugin {
  private MenuManager menuManager;
  private final Map<UUID, EditorSession> editorSessions = new HashMap<>();
  private final Map<UUID, PendingInput> pendingAnvilInputs = new HashMap<>();
  private final Map<UUID, PendingInput> pendingChatInputs = new HashMap<>();

  @Override
  public void onEnable() {
    menuManager = new MenuManager(this);
    MenuListener listener = new MenuListener(this);
    Bukkit.getPluginManager().registerEvents(listener, this);

    PluginCommand regmenu = getCommand("regmenu");
    if (regmenu != null) {
      RegMenuCommand regMenuCommand = new RegMenuCommand(this);
      regmenu.setExecutor(regMenuCommand);
      regmenu.setTabCompleter(regMenuCommand);
    }

    PluginCommand openmenu = getCommand("openmenu");
    if (openmenu != null) {
      OpenMenuCommand openMenuCommand = new OpenMenuCommand(this);
      openmenu.setExecutor(openMenuCommand);
      openmenu.setTabCompleter(openMenuCommand);
    }
  }

  public MenuManager getMenuManager() {
    return menuManager;
  }

  public Map<UUID, EditorSession> getEditorSessions() {
    return editorSessions;
  }

  public Map<UUID, PendingInput> getPendingAnvilInputs() {
    return pendingAnvilInputs;
  }

  public Map<UUID, PendingInput> getPendingChatInputs() {
    return pendingChatInputs;
  }
}
