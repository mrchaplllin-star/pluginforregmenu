package com.example.regmenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegMenuCommand implements CommandExecutor, TabCompleter {
  private final RegMenuPlugin plugin;

  public RegMenuCommand(RegMenuPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                           @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(ChatColor.RED + "Цю команду можуть використовувати лише гравці.");
      return true;
    }
    if (!player.hasPermission("regmenu.edit")) {
      player.sendMessage(ChatColor.RED + "У вас немає прав на редагування меню.");
      return true;
    }
    if (args.length < 2) {
      sendUsage(player);
      return true;
    }

    String sub = args[0].toLowerCase();
    String menuName = args[1];
    MenuManager menuManager = plugin.getMenuManager();
    MenuData menu = menuManager.getOrCreateMenu(menuName);

    switch (sub) {
      case "create" -> handleCreate(player, menu, args);
      case "decor" -> openEditor(player, menu);
      case "name" -> handleRename(player, menu, args);
      case "namemenu" -> handleMenuName(player, menu, args);
      case "command" -> handleCommandAssign(player, menu, args);
      case "setitem" -> handleSetItem(player, menu, args);
      case "itemchar" -> handleItemCharm(player, menu, args);
      case "lore" -> handleLore(player, menu, args);
      case "opencommand" -> handleOpenCommand(player, menu, args);
      default -> sendUsage(player);
    }
    return true;
  }

  private void handleCreate(Player player, MenuData menu, String[] args) {
    if (args.length < 3) {
      player.sendMessage(ChatColor.RED + "Використання: /regmenu create <меню> <тип> [scroll]");
      return;
    }
    String typeId = args[2].toLowerCase();
    if (!isValidType(typeId)) {
      player.sendMessage(ChatColor.RED + "Невірний тип. Використовуйте: chest, large_chest, ender_chest, barrel, shulker_color");
      return;
    }
    boolean scrollEnabled = args.length >= 4 && args[3].equalsIgnoreCase("scroll");
    MenuInventoryType type = MenuInventoryType.fromId(typeId);
    MenuData updated = new MenuData(menu.getName(), type.getSize(), type, typeId);
    for (var entry : menu.getItems().entrySet()) {
      if (entry.getKey() >= 0 && entry.getKey() < updated.getSize()) {
        updated.setItem(entry.getKey(), entry.getValue());
      }
    }
    updated.setScrollEnabled(scrollEnabled);
    updated.getOpenCommands().addAll(menu.getOpenCommands());
    plugin.getMenuManager().getMenus().put(plugin.getMenuManager().normalizeName(menu.getName()), updated);
    plugin.getMenuManager().saveMenu(updated);
    player.sendMessage(ChatColor.GREEN + "Меню створено: " + updated.getName() + " (" + typeId + ")");
  }

  private void openEditor(Player player, MenuData menu) {
    org.bukkit.inventory.Inventory inventory;
    String title = MenuTitleFormatter.format(menu);
    if (menu.getInventoryType().getInventoryType() == null) {
      inventory = plugin.getServer().createInventory(new MenuHolder(menu.getName(), MenuHolder.Mode.EDIT),
          menu.getSize(), title);
    } else {
      inventory = plugin.getServer().createInventory(new MenuHolder(menu.getName(), MenuHolder.Mode.EDIT),
          menu.getInventoryType().getInventoryType(), title);
    }
    menu.getItems().forEach((slot, item) -> {
      if (slot >= 0 && slot < inventory.getSize()) {
        inventory.setItem(slot, item.getItemStack());
      }
    });
    fillEmptySlotsIfNew(menu, inventory);
    plugin.getEditorSessions().put(player.getUniqueId(), new EditorSession(menu.getName(), inventory));
    player.openInventory(inventory);
    player.sendMessage(ChatColor.GREEN + "Редагування меню: " + menu.getName());
  }

  private void fillEmptySlotsIfNew(MenuData menu, org.bukkit.inventory.Inventory inventory) {
    if (!menu.getItems().isEmpty()) {
      return;
    }
    for (int slot = 0; slot < inventory.getSize(); slot++) {
      if (inventory.getItem(slot) == null || inventory.getItem(slot).getType() == org.bukkit.Material.AIR) {
        inventory.setItem(slot, MenuItemUtils.createFiller(plugin));
      }
    }
  }

  private void handleRename(Player player, MenuData menu, String[] args) {
    if (args.length < 4) {
      player.sendMessage(ChatColor.RED + "Використання: /regmenu name <меню> <слот> <назва>");
      return;
    }
    int slot = parseSlot(player, args[2], menu.getSize());
    if (slot < 0) {
      return;
    }
    MenuItemData itemData = resolveItemData(player, menu, slot);
    if (itemData == null || itemData.getItemStack() == null) {
      player.sendMessage(ChatColor.RED + "Цей слот порожній у цьому меню.");
      return;
    }
    String name = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
    itemData.applyName(ChatColor.translateAlternateColorCodes('&', name));
    menu.setItem(slot, itemData);
    plugin.getMenuManager().saveMenu(menu);
    updateEditorSlot(player, menu, slot, itemData.getItemStack());
    player.sendMessage(ChatColor.GREEN + "Назву оновлено.");
  }

  private void handleMenuName(Player player, MenuData menu, String[] args) {
    if (args.length < 3) {
      player.sendMessage(ChatColor.RED + "Використання: /regmenu namemenu <меню> <назва> АБО /regmenu namemenu <меню> <left|center|right> <назва>");
      return;
    }
    String alignmentCandidate = args[2].toLowerCase();
    MenuTitleAlignment alignment = MenuTitleAlignment.fromString(alignmentCandidate);
    int startIndex = 2;
    if (alignmentCandidate.equals("left") || alignmentCandidate.equals("center") || alignmentCandidate.equals("right")) {
      startIndex = 3;
    } else {
      alignment = MenuTitleAlignment.LEFT;
    }
    if (args.length <= startIndex) {
      player.sendMessage(ChatColor.RED + "Використання: /regmenu namemenu <меню> <назва> АБО /regmenu namemenu <меню> <left|center|right> <назва>");
      return;
    }
    String name = String.join(" ", java.util.Arrays.copyOfRange(args, startIndex, args.length));
    menu.setTitle(ChatColor.translateAlternateColorCodes('&', name));
    menu.setTitleAlignment(alignment);
    plugin.getMenuManager().saveMenu(menu);
    player.sendMessage(ChatColor.GREEN + "Назву меню оновлено.");
  }

  private void handleCommandAssign(Player player, MenuData menu, String[] args) {
    if (args.length < 5) {
      player.sendMessage(ChatColor.RED + "Використання: /regmenu command <меню> <слот> <player|console> <команда>");
      return;
    }
    int slot = parseSlot(player, args[2], menu.getSize());
    if (slot < 0) {
      return;
    }
    MenuItemData itemData = resolveItemData(player, menu, slot);
    if (itemData == null || itemData.getItemStack() == null) {
      player.sendMessage(ChatColor.RED + "Цей слот порожній у цьому меню.");
      return;
    }
    CommandExecutorType executor = CommandExecutorType.fromString(args[3]);
    String command = String.join(" ", java.util.Arrays.copyOfRange(args, 4, args.length));
    itemData.setCommand(command);
    itemData.setExecutor(executor);
    menu.setItem(slot, itemData);
    plugin.getMenuManager().saveMenu(menu);
    updateEditorSlot(player, menu, slot, itemData.getItemStack());
    player.sendMessage(ChatColor.GREEN + "Команду призначено.");
  }

  private void handleSetItem(Player player, MenuData menu, String[] args) {
    if (args.length < 3) {
      player.sendMessage(ChatColor.RED + "Використання: /regmenu setitem <меню> <назва> [lock] [hotbar|hodbar|invent]");
      return;
    }
    ItemStack item = player.getInventory().getItemInMainHand();
    if (item.getType() == Material.AIR) {
      player.sendMessage(ChatColor.RED + "Спочатку візьміть предмет у головну руку.");
      return;
    }
    boolean lock = false;
    LinkedMenuMode mode = LinkedMenuMode.INVENT;
    int nameEnd = args.length;
    while (nameEnd > 2) {
      String candidate = args[nameEnd - 1].toLowerCase();
      if (candidate.equals("lock")) {
        lock = true;
        nameEnd -= 1;
        continue;
      }
      if (candidate.equals("hotbar") || candidate.equals("hodbar") || candidate.equals("invent")) {
        mode = LinkedMenuMode.fromString(candidate);
        nameEnd -= 1;
        continue;
      }
      break;
    }
    if (nameEnd <= 2) {
      player.sendMessage(ChatColor.RED + "Використання: /regmenu setitem <меню> <назва> [lock] [hotbar|hodbar|invent]");
      return;
    }
    String name = String.join(" ", Arrays.copyOfRange(args, 2, nameEnd));
    ItemStack updated = item.clone();
    var meta = updated.getItemMeta();
    if (meta != null) {
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
      updated.setItemMeta(meta);
    }
    MenuItemUtils.setLocked(plugin, updated, lock);
    MenuItemUtils.setLinkedMenu(plugin, updated, menu.getName());
    MenuItemUtils.setLinkedMenuMode(plugin, updated, mode);
    player.getInventory().setItemInMainHand(updated);
    MenuInventoryFactory.openMenu(plugin, player, menu);
  }

  private void handleItemCharm(Player player, MenuData menu, String[] args) {
    if (args.length < 4) {
      player.sendMessage(ChatColor.RED + "Використання: /regmenu itemchar <меню> <слот> <true|false>");
      return;
    }
    int slot = parseSlot(player, args[2], menu.getSize());
    if (slot < 0) {
      return;
    }
    MenuItemData itemData = resolveItemData(player, menu, slot);
    if (itemData == null || itemData.getItemStack() == null) {
      player.sendMessage(ChatColor.RED + "Цей слот порожній у цьому меню.");
      return;
    }
    boolean enabled = Boolean.parseBoolean(args[3]);
    itemData.setGlint(enabled);
    menu.setItem(slot, itemData);
    plugin.getMenuManager().saveMenu(menu);
    updateEditorSlot(player, menu, slot, itemData.getItemStack());
    player.sendMessage(ChatColor.GREEN + "Блиск предмета оновлено.");
  }

  private void handleLore(Player player, MenuData menu, String[] args) {
    if (args.length < 4) {
      player.sendMessage(ChatColor.RED + "Використання: /regmenu lore <меню> <слот> <текст>");
      return;
    }
    int slot = parseSlot(player, args[2], menu.getSize());
    if (slot < 0) {
      return;
    }
    MenuItemData itemData = resolveItemData(player, menu, slot);
    if (itemData == null || itemData.getItemStack() == null) {
      player.sendMessage(ChatColor.RED + "Цей слот порожній у цьому меню.");
      return;
    }
    String lore = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
    itemData.applyLore(ChatColor.translateAlternateColorCodes('&', lore));
    menu.setItem(slot, itemData);
    plugin.getMenuManager().saveMenu(menu);
    updateEditorSlot(player, menu, slot, itemData.getItemStack());
    player.sendMessage(ChatColor.GREEN + "Опис предмета оновлено.");
  }

  private void handleOpenCommand(Player player, MenuData menu, String[] args) {
    if (args.length < 4) {
      player.sendMessage(ChatColor.RED + "Використання: /regmenu opencommand <add|remove> <меню> <команда>");
      return;
    }
    String action = args[2].toLowerCase();
    String command = args[3];
    String normalized = plugin.getOpenCommandRegistrar().normalize(command);
    if (normalized.isBlank()) {
      player.sendMessage(ChatColor.RED + "Команда не може бути порожньою.");
      return;
    }
    if (action.equals("add")) {
      if (menu.getOpenCommands().contains(normalized)) {
        player.sendMessage(ChatColor.RED + "Така команда відкриття вже існує.");
        return;
      }
      menu.getOpenCommands().add(normalized);
      plugin.getMenuManager().saveMenu(menu);
      plugin.getOpenCommandRegistrar().registerCommand(menu.getName(), normalized);
      player.sendMessage(ChatColor.GREEN + "Команду відкриття додано: /" + normalized);
      return;
    }
    if (action.equals("remove")) {
      if (!menu.getOpenCommands().remove(normalized)) {
        player.sendMessage(ChatColor.RED + "Такої команди відкриття не існує.");
        return;
      }
      plugin.getMenuManager().saveMenu(menu);
      plugin.getOpenCommandRegistrar().unregisterCommand(normalized);
      player.sendMessage(ChatColor.GREEN + "Команду відкриття видалено: /" + normalized);
      return;
    }
    player.sendMessage(ChatColor.RED + "Використання: /regmenu opencommand <add|remove> <меню> <команда>");
  }

  private int parseSlot(Player player, String slotArg, int size) {
    try {
      int slot = Integer.parseInt(slotArg);
      if (slot < 1 || slot > size) {
        player.sendMessage(ChatColor.RED + "Слот має бути від 1 до " + size + ".");
        return -1;
      }
      return slot - 1;
    } catch (NumberFormatException ex) {
      player.sendMessage(ChatColor.RED + "Слот має бути числом.");
      return -1;
    }
  }

  private void sendUsage(Player player) {
    player.sendMessage(ChatColor.YELLOW + "Використання:");
    player.sendMessage(ChatColor.GRAY + "/regmenu create <меню> <тип> [scroll]");
    player.sendMessage(ChatColor.GRAY + "/regmenu decor <меню>");
    player.sendMessage(ChatColor.GRAY + "/regmenu name <меню> <слот> <назва>");
    player.sendMessage(ChatColor.GRAY + "/regmenu namemenu <меню> <назва>");
    player.sendMessage(ChatColor.GRAY + "/regmenu namemenu <меню> <left|center|right> <назва>");
    player.sendMessage(ChatColor.GRAY + "/regmenu command <меню> <слот> <player|console> <команда>");
    player.sendMessage(ChatColor.GRAY + "/regmenu setitem <меню> <назва> [lock] [hotbar|hodbar|invent]");
    player.sendMessage(ChatColor.GRAY + "/regmenu lore <меню> <слот> <текст>");
    player.sendMessage(ChatColor.GRAY + "/regmenu itemchar <меню> <слот> <true|false>");
    player.sendMessage(ChatColor.GRAY + "/regmenu opencommand <add|remove> <меню> <команда>");
  }

  private boolean isValidType(String typeId) {
    if (typeId == null) {
      return false;
    }
    if (typeId.startsWith("shulker_")) {
      return true;
    }
    return typeId.equals("chest")
        || typeId.equals("large_chest")
        || typeId.equals("ender_chest")
        || typeId.equals("barrel");
  }

  private MenuItemData resolveItemData(Player player, MenuData menu, int slot) {
    MenuItemData itemData = menu.getItem(slot);
    if (itemData != null && itemData.getItemStack() != null) {
      return itemData;
    }
    EditorSession session = plugin.getEditorSessions().get(player.getUniqueId());
    if (session != null && session.getMenuName().equalsIgnoreCase(menu.getName())) {
      ItemStack item = session.getInventory().getItem(slot);
      if (item != null && item.getType() != Material.AIR && !MenuItemUtils.isFiller(plugin, item)) {
        MenuItemData newData = new MenuItemData(item.clone());
        menu.setItem(slot, newData);
        return newData;
      }
    }
    return null;
  }

  private void updateEditorSlot(Player player, MenuData menu, int slot, ItemStack itemStack) {
    EditorSession session = plugin.getEditorSessions().get(player.getUniqueId());
    if (session != null && session.getMenuName().equalsIgnoreCase(menu.getName())) {
      session.getInventory().setItem(slot, itemStack);
    }
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                              @NotNull String alias, @NotNull String[] args) {
    if (args.length == 1) {
      return Arrays.asList("create", "decor", "name", "namemenu", "command", "setitem", "lore", "itemchar", "opencommand");
    }
    if (args.length == 2) {
      return plugin.getMenuManager().getMenus().keySet().stream().toList();
    }
    if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
      return Arrays.asList("chest", "large_chest", "ender_chest", "barrel", "shulker_red");
    }
    if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
      return Arrays.asList("scroll");
    }
    if (args.length == 3 && args[0].equalsIgnoreCase("opencommand")) {
      return Arrays.asList("add", "remove");
    }
    if (args.length == 3 && args[0].equalsIgnoreCase("namemenu")) {
      return Arrays.asList("left", "center", "right");
    }
    if (args.length == 4 && args[0].equalsIgnoreCase("command")) {
      return Arrays.asList("player", "console");
    }
    if (args.length == 4 && args[0].equalsIgnoreCase("itemchar")) {
      return Arrays.asList("true", "false");
    }
    if (args.length >= 4 && args[0].equalsIgnoreCase("setitem")) {
      return Arrays.asList("lock", "hotbar", "hodbar", "invent");
    }
    return new ArrayList<>();
  }
}
