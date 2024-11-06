package snw.srs.gui;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Range;

import java.util.OptionalInt;
import java.util.function.Supplier;

import static snw.srs.gui.GUISharedObjects.NEXT_PAGE_BUTTON;
import static snw.srs.gui.GUISharedObjects.PREV_PAGE_BUTTON;

public final class GUIPageHelper {
    private final AbstractPluginGUI owner;
    private final Supplier<OptionalInt> maxPageGetter;
    @Getter
    @Setter
    private int page = 1;

    public GUIPageHelper(AbstractPluginGUI owner) {
        this(owner, null);
    }

    public GUIPageHelper(AbstractPluginGUI owner, Supplier<OptionalInt> maxPageGetter) {
        this.owner = owner;
        this.maxPageGetter = maxPageGetter;
    }

    public void nextPageAndScheduleDraw() {
        setPageAndScheduleDraw(getPage() + 1);
    }

    public void prevPageAndScheduleDraw() {
        setPageAndScheduleDraw(getPage() - 1);
    }

    // use this when we are in InventoryClickEvent
    public void setPageAndScheduleDraw(int page) {
        setPage(page, false);
    }

    public void setPage(@Range(from = 1, to = Integer.MAX_VALUE) int page, boolean drawImmediately) {
        if (this.page != page) {
            OptionalInt maxPage = getMaxPage();
            if (maxPage.isPresent()) {
                if (page > maxPage.getAsInt()) {
                    return;
                }
            }
            this.page = page;
            if (drawImmediately) {
                owner.drawImmediately();
            } else {
                owner.scheduleDraw();
            }
        }
    }

    public OptionalInt getMaxPage() {
        if (maxPageGetter != null) {
            return maxPageGetter.get();
        }
        if (owner instanceof AbstractPagedGUI paged) {
            return paged.getMaxPage();
        }
        return OptionalInt.empty();
    }

    public void drawButtons() {
        GUIButtonHelper helper = owner.getButtonHelper();
        if (page > 1) {
            helper.setButton(48, owner.buildTranslated(PREV_PAGE_BUTTON), (clicker, clickType) -> {
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
            helper.setButton(50, owner.buildTranslated(NEXT_PAGE_BUTTON), (clicker, clickType) -> {
                nextPageAndScheduleDraw();
                return GUIClickResult.CANCEL_CLICK;
            });
        }
    }
}
