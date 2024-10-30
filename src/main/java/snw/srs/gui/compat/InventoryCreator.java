package snw.srs.gui.compat;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public abstract class InventoryCreator {
    public abstract Inventory createInventory(InventoryHolder holder, int size, Component title);

    public static InventoryCreator getInventoryCreator() {
        return Singleton.INSTANCE;
    }

    private static final class Singleton {
        private static final InventoryCreator INSTANCE;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodType type = MethodType.methodType(Inventory.class, InventoryHolder.class, int.class, Component.class);
            boolean isPaper = true;
            try {
                lookup.findStatic(Bukkit.class, "createInventory", type);
            } catch (NoSuchMethodException | IllegalAccessException e) { // the second one will never happen
                isPaper = false; // oh, Spigot or lower
            }
            if (isPaper) {
                INSTANCE = new PaperInventoryCreator();
            } else {
                INSTANCE = new SpigotInventoryCreator();
            }
        }
    }
}
