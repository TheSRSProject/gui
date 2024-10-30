package snw.srs.gui;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Range;

import java.util.OptionalInt;
import java.util.UUID;

import static snw.srs.gui.GUISharedObjects.NEXT_PAGE_BUTTON;
import static snw.srs.gui.GUISharedObjects.PREV_PAGE_BUTTON;

@Getter
public abstract class AbstractPagedGUI extends AbstractPluginGUI {
    private int page = 1;

    @Deprecated
    protected AbstractPagedGUI(Plugin plugin, String title, UUID viewer) {
        super(plugin, title, viewer);
    }

    protected AbstractPagedGUI(Plugin plugin, Component title, UUID viewer) {
        super(plugin, title, viewer);
    }

    public final void nextPageAndScheduleDraw() {
        setPageAndScheduleDraw(getPage() + 1);
    }

    public final void prevPageAndScheduleDraw() {
        setPageAndScheduleDraw(getPage() - 1);
    }

    // use this when we are in InventoryClickEvent
    public final void setPageAndScheduleDraw(int page) {
        setPage(page, false);
    }

    public final void setPage(@Range(from = 1, to = Integer.MAX_VALUE) int page, boolean drawImmediately) {
        if (this.page != page) {
            OptionalInt maxPage = getMaxPage();
            if (maxPage.isPresent()) {
                if (page > maxPage.getAsInt()) {
                    return;
                }
            }
            this.page = page;
            if (drawImmediately) {
                drawImmediately();
            } else {
                scheduleDraw();
            }
        }
    }

    public abstract OptionalInt getMaxPage();

    @Override
    protected final void drawImmediatelyImpl() {
        drawImmediatelyImpl(getPage());
    }

    protected abstract void drawImmediatelyImpl(int page);

    @Override
    public GUIClickResult handleClick(Player clicker, int slot, ClickType clickType) {
        GUIClickResult superResult = super.handleClick(clicker, slot, clickType);
        if (superResult != GUIClickResult.NOP) {
            return superResult;
        }
        return getButtonHelper().handleClick(clicker, slot, clickType);
    }

    @Override
    public void drawButtons() {
        super.drawButtons();
        GUIButtonHelper helper = getButtonHelper();
        if (page > 1) {
            helper.setButton(48, buildTranslated(PREV_PAGE_BUTTON), (clicker, clickType) -> {
                prevPageAndScheduleDraw();
                return GUIClickResult.CANCEL_CLICK;
            });
        }
        final boolean shouldPutNextPage;
        OptionalInt maxPageOpt = getMaxPage();
        if (maxPageOpt.isPresent()) {
            shouldPutNextPage = maxPageOpt.getAsInt() > page;
        } else {
            shouldPutNextPage = true;
        }
        if (shouldPutNextPage) {
            helper.setButton(50, buildTranslated(NEXT_PAGE_BUTTON), (clicker, clickType) -> {
                nextPageAndScheduleDraw();
                return GUIClickResult.CANCEL_CLICK;
            });
        }
    }
}
