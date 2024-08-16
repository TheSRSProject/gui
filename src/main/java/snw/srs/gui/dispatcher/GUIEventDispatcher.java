package snw.srs.gui.dispatcher;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import snw.srs.gui.AbstractPluginGUI;
import snw.srs.gui.GUIButtonHelper;
import snw.srs.gui.GUIClickResult;

import java.util.Objects;

public final class GUIEventDispatcher implements Listener {
    private final Plugin plugin;

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
}
