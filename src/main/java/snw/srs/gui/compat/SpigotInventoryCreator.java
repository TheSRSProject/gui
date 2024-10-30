package snw.srs.gui.compat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

final class SpigotInventoryCreator extends InventoryCreator {
    @Override
    public Inventory createInventory(InventoryHolder holder, int size, Component title) {
        String asLegacy = LegacyComponentSerializer.legacyAmpersand().serialize(title);
        // We had no way as we're on Spigot
        // noinspection deprecation
        return Bukkit.createInventory(holder, size, asLegacy);
    }
}
