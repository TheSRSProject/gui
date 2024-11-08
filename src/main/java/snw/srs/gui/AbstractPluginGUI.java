package snw.srs.gui;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import snw.srs.gui.compat.InventoryCreator;
import snw.srs.gui.interfaces.Disposable;
import snw.srs.gui.util.TranslatedItem;
import snw.srs.i18n.bukkit.BukkitI18NEngine;

import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static snw.srs.gui.GUISharedObjects.FRAME_ITEM;

public abstract class AbstractPluginGUI implements InventoryHolder, Disposable {
    @Getter
    private final Plugin plugin;
    @Getter
    private final UUID viewer;
    @Getter
    private final BukkitI18NEngine i18nEngine;
    @Getter
    private final int size;
    private Inventory handle;
    @Getter
    private boolean disposed = false;
    @Getter
    private boolean reopening = false;
    private boolean firstOpen = true;
    private GUIButtonHelper buttonHelper;

    @Deprecated
    protected AbstractPluginGUI(Plugin plugin, String title, UUID viewer) {
        this(plugin, text(title), viewer);
    }

    @Deprecated
    protected AbstractPluginGUI(Plugin plugin, String title, UUID viewer, int size) {
        this(plugin, text(title), viewer, size);
    }

    protected AbstractPluginGUI(Plugin plugin, Component title, UUID viewer) {
        this(plugin, title, viewer, 54);
    }

    protected AbstractPluginGUI(Plugin plugin, Component title, UUID viewer, int size) {
        this.plugin = plugin;
        this.i18nEngine = GUII18NEngineBoard.getOrCreate(plugin);
        this.viewer = viewer;
        this.size = size;
        GUIEventDispatcher.registerIfNeededFor(plugin);
        GUIEventDispatcher.activeGUIs.put(plugin.getName(), this);
        resetHandle(title);
    }

    protected void reopen(Runnable beforeReopen) {
        reopening = true;
        close();
        beforeReopen.run();
        Player viewerHandle = Bukkit.getPlayer(viewer);
        if (viewerHandle != null) {
            viewerHandle.openInventory(getInventory());
        }
        reopening = false;
    }

    private void resetHandle(Component title) {
        this.handle = InventoryCreator.getInventoryCreator().createInventory(this, this.size, title);
    }

    @Deprecated
    public void setTitle(String title) {
        setTitle(LegacyComponentSerializer.legacySection().deserialize(title));
    }

    public void setTitle(Component title) {
        reopen(() -> resetHandle(title));
    }

    public boolean show() {
        if (disposed) {
            return false;
        }
        Player viewerHandle = Bukkit.getPlayer(viewer);
        if (viewerHandle == null) {
            return false;
        }
        if (handle.equals(viewerHandle.getOpenInventory().getTopInventory())) { // reopening
            viewerHandle.closeInventory();
        }
        if (firstOpen) {
            drawImmediately(); // init the gui when it is being displayed for the first time
            firstOpen = false;
        }
        viewerHandle.openInventory(getInventory());
        return true;
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
        if (buttonHelper != null) {
            return buttonHelper.handleClick(clicker, slot, clickType);
        }
        return GUIClickResult.NOP;
    }

    public void handleClose() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player viewerHandle = Bukkit.getPlayer(viewer);
            if (viewerHandle != null) {
                viewerHandle.updateInventory();
            }
        }, 1L);
    }

    public void dispose(boolean closeInvImmediately) {
        if (disposed) {
            return;
        }
        disposed = true;
        Runnable closeOp = () -> {
            close();
            clear();
            GUIEventDispatcher.activeGUIs.remove(getPlugin().getName(), this);
        };
        if (closeInvImmediately) {
            closeOp.run();
        } else {
            getPlugin().getServer().getScheduler().runTask(getPlugin(), closeOp);
        }
    }

    protected void close() {
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

    protected ItemStack buildTranslated(TranslatedItem item) {
        Player viewerHandle = Bukkit.getPlayer(viewer);
        assert viewerHandle != null;
        return item.buildFor(viewerHandle, i18nEngine);
    }
}
