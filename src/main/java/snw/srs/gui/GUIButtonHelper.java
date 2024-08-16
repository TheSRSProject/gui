package snw.srs.gui;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import snw.srs.gui.interfaces.Disposable;

public final class GUIButtonHelper implements Disposable {
    private final AbstractPluginGUI handle;
    private final Int2ObjectMap<GUIButtonCallback> slotCallbacks = new Int2ObjectOpenHashMap<>();
    private final IntSet justItemSlotSet = new IntArraySet();
    private boolean disposed = false;

    public GUIButtonHelper(AbstractPluginGUI handle) {
        this.handle = handle;
    }

    public GUIClickResult handleClick(Player clicker, int slot, ClickType clickType) {
        if (disposed) {
            return GUIClickResult.NOP;
        }
        if (justItemSlotSet.contains(slot)) {
            return GUIClickResult.ALLOW_CLICK;
        } else {
            GUIButtonCallback callback = slotCallbacks.get(slot);
            if (callback != null) {
                return callback.handle(clicker, clickType);
            } else {
                return GUIClickResult.NOP;
            }
        }
    }

    public void setButton(int index, ItemStack item, GUIButtonCallback callback) {
        justItemSlotSet.remove(index);
        slotCallbacks.put(index, callback);
        handle.getInventory().setItem(index, item);
    }

    public void setJustItem(int index, ItemStack item) {
        slotCallbacks.remove(index);
        justItemSlotSet.add(index);
        handle.getInventory().setItem(index, item);
    }

    public void setBlockedSlot(int slot, ItemStack item) {
        setButton(slot, item, (clicker, clickType) -> GUIClickResult.CANCEL_CLICK);
    }

    public boolean isJustItemSlot(int slot) {
        return justItemSlotSet.contains(slot);
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        reset();
    }

    public void reset() {
        slotCallbacks.clear();
        justItemSlotSet.clear();
    }
}
