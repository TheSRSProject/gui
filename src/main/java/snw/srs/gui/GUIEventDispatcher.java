package snw.srs.gui;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class GUIEventDispatcher implements Listener {
    private static final Set<String> registeredPlugins;
    static final Multimap<String, AbstractPluginGUI> activeGUIs;
    private final Plugin plugin;

    static {
        registeredPlugins = new HashSet<>();
        activeGUIs = ArrayListMultimap.create();
    }

    public static void registerIfNeededFor(Plugin plugin) {
        String name = plugin.getName();
        if (!registeredPlugins.contains(name)) {
            plugin.getServer().getPluginManager().registerEvents(new GUIEventDispatcher(plugin), plugin);
            registeredPlugins.add(name);
        }
    }

    // You will have no need to call this by yourself or there is a bug.
    @ApiStatus.Internal
    public GUIEventDispatcher(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof AbstractPluginGUI gui) {
            if (!gui.getPlugin().equals(plugin)) {
                return;
            }
            if (e.getPlayer() instanceof Player viewer) {
                gui.handleClose(viewer);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof AbstractPluginGUI gui) {
            if (!gui.getPlugin().equals(plugin)) {
                return;
            }
            boolean cancel;
            final GUIButtonHelper helper = gui.getButtonHelperOptionally().orElse(null);
            if (helper != null) {
                cancel = !e.getInventorySlots().stream().allMatch(helper::isJustItemSlot);
            } else {
                cancel = true;
            }
            if (cancel) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        Inventory clickedInv = e.getClickedInventory();
        if (clickedInv == null) {
            return;
        }
        if (e.getView().getTopInventory().getHolder() instanceof AbstractPluginGUI gui) {
            if (!gui.getPlugin().equals(plugin)) {
                return;
            }
            if (gui.isDisposed()) { // rarely happen but must prevent it
                return;
            }
            if (!Objects.equals(clickedInv, gui.getInventory())) {
                e.setCancelled(true);
                return;
            }
            if (e.getWhoClicked() instanceof Player clicker) { // or GameTest in Bukkit?
                int slot = e.getSlot();
                ClickType clickType = e.getClick();
                GUIClickResult result = gui.handleClick(clicker, slot, clickType);
                if (result != GUIClickResult.ALLOW_CLICK) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    // We are able to listen for any disabling plugin even ourselves
    // According to Bukkit API code, we have the last chance to do cleanup
    // before we're being fully disabled.
    public void onPluginDisable(PluginDisableEvent event) {
        String name = event.getPlugin().getName();
        boolean removed = registeredPlugins.remove(name);
        if (removed) {
            Collection<AbstractPluginGUI> pluginActiveGUIs;
            pluginActiveGUIs = GUIEventDispatcher.activeGUIs.removeAll(name);
            for (AbstractPluginGUI gui : pluginActiveGUIs) {
                // We must close this immediately as we will not be able to schedule tasks
                gui.dispose(true);
            }
        }
    }
}
