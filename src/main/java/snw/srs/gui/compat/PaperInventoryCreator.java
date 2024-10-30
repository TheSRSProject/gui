package snw.srs.gui.compat;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

final class PaperInventoryCreator extends InventoryCreator {
    @Override
    public Inventory createInventory(InventoryHolder holder, int size, Component title) {
        return Bukkit.createInventory(holder, size, title);
    }
}
