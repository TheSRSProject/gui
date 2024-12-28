package snw.srs.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Range;

import java.util.OptionalInt;
import java.util.UUID;

public abstract class AbstractPagedGUI extends AbstractPluginGUI {
    private final GUIPageHelper pageHelper;

    @Deprecated
    protected AbstractPagedGUI(Plugin plugin, String title, UUID viewer) {
        super(plugin, title, viewer);
        this.pageHelper = new GUIPageHelper(this);
    }

    protected AbstractPagedGUI(Plugin plugin, Component title, UUID viewer) {
        super(plugin, title, viewer);
        this.pageHelper = new GUIPageHelper(this);
    }

    public int getPage() {
        return pageHelper.getPage();
    }

    public final void nextPageAndScheduleDraw() {
        pageHelper.nextPageAndScheduleDraw();
    }

    public final void prevPageAndScheduleDraw() {
        pageHelper.prevPageAndScheduleDraw();
    }

    // use this when we are in InventoryClickEvent
    public final void setPageAndScheduleDraw(int page) {
        pageHelper.setPage(page, false);
    }

    public final void setPage(@Range(from = 1, to = Integer.MAX_VALUE) int page, boolean drawImmediately) {
        pageHelper.setPage(page, drawImmediately);
    }

    public abstract OptionalInt getMaxPage();

    @Override
    protected final void drawImmediatelyImpl() {
        drawImmediatelyImpl(getPage());
    }

    protected abstract void drawImmediatelyImpl(int page);

    @Override
    protected void drawButtons() {
        super.drawButtons();
        pageHelper.drawButtons();
    }
}
