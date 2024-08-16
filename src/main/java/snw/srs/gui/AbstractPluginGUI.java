package snw.srs.gui;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import snw.srs.gui.interfaces.Disposable;

import java.util.Optional;

import static snw.srs.gui.GUISharedObjects.FRAME_ITEM;

public abstract class AbstractPluginGUI implements InventoryHolder, Disposable {
    @Getter
    private final Plugin plugin;
    private Inventory handle;
    @Getter
    private boolean disposed = false;
    @Getter
    private boolean reopening = false;
    private boolean firstOpen = true;
    private GUIButtonHelper buttonHelper;

    protected AbstractPluginGUI(Plugin plugin, String title) {
        this.plugin = plugin;
        resetHandle(title);
    }

    protected void reopenForAll(Runnable beforeReopen) {
        reopening = true;
        ImmutableList<HumanEntity> viewers = closeForAll();
        beforeReopen.run();
        viewers.forEach(this::showTo);
        reopening = false;
    }

    private void resetHandle(String title) {
        this.handle = Bukkit.createInventory(this, 54, title);
    }

    public void setTitle(String title) {
        reopenForAll(() -> resetHandle(title));
    }

    public void showTo(HumanEntity player) {
        if (disposed) {
            return;
        }
        if (handle.equals(player.getOpenInventory().getTopInventory())) { // reopening
            player.closeInventory();
        }
        if (firstOpen) {
            drawImmediately(); // init the gui when it is being displayed for the first time
            firstOpen = false;
        }
        player.openInventory(getInventory());
    }

    // call this instead if we are in handleClick method
    protected final void scheduleDraw() {
        getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), this::drawImmediately, 1L);
    }

    protected void drawImmediately() {
        clear();
        drawImmediatelyImpl();
    }

    // draw the GUI immediately, DO NOT work correctly when we are in InventoryClickEvent
    protected abstract void drawImmediatelyImpl();

    public GUIClickResult handleClick(Player clicker, int slot, ClickType clickType) {
        if (disposed) {
            return GUIClickResult.CANCEL_CLICK; // invalid click
        }
        return GUIClickResult.NOP;
    }

    public abstract void handleClose(Player viewer);

    public void dispose(boolean closeInvImmediately) {
        if (disposed) {
            return;
        }
        disposed = true;
        Runnable closeOp = () -> {
            closeForAll();
            clear();
        };
        if (closeInvImmediately) {
            closeOp.run();
        } else {
            getPlugin().getServer().getScheduler().runTask(getPlugin(), closeOp);
        }
    }

    protected ImmutableList<HumanEntity> closeForAll() {
        ImmutableList<HumanEntity> viewers = ImmutableList.copyOf(handle.getViewers());
        for (HumanEntity viewer : viewers) {
            viewer.closeInventory();
            if (viewer instanceof Player player) { // or GameTest in Bukkit???
                // Necessary because players might be able to create fake items
                // Fake item does not usable in survival, but usable under creative mode
                // We're not sure if it is a bug.
                // noinspection UnstableApiUsage
                player.updateInventory(); // remove fake items taken from the GUI
            }
        }
        return viewers;
    }

    public void clear() {
        getInventory().clear();
        if (buttonHelper != null) {
            buttonHelper.reset();
        }
    }

    @Override
    public void dispose() {
        dispose(true);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return handle;
    }

    protected void drawFrame() {
        int[] frameSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        GUIButtonHelper helper = getButtonHelper();
        for (int slot : frameSlots) {
            helper.setBlockedSlot(slot, FRAME_ITEM);
        }
    }

    protected void drawButtons() {
    }

    // as it is not necessary for all GUIs, we delay its initialization there
    protected GUIButtonHelper getButtonHelper() {
        if (buttonHelper == null) {
            buttonHelper = new GUIButtonHelper(this);
        }
        return buttonHelper;
    }

    public Optional<GUIButtonHelper> getButtonHelperOptionally() {
        return Optional.ofNullable(buttonHelper);
    }
}
