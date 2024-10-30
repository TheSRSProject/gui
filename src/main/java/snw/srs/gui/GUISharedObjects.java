package snw.srs.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import snw.srs.gui.util.ItemBuilder;
import snw.srs.gui.util.TranslatedItem;

import static snw.srs.gui.i18n.TranslationKeys.*;

public final class GUISharedObjects {
    public static final ItemStack FRAME_ITEM;
    public static final TranslatedItem CANCEL_BUTTON;
    public static final TranslatedItem PREV_PAGE_BUTTON;
    public static final TranslatedItem NEXT_PAGE_BUTTON;
    public static final TranslatedItem BACK_BUTTON;

    static {
        FRAME_ITEM = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("").build();
        CANCEL_BUTTON = new TranslatedItem(GENERIC_CANCEL, Material.BARRIER, ChatColor.RED);
        PREV_PAGE_BUTTON = new TranslatedItem(GENERIC_PREVIOUS_PAGE, Material.ARROW);
        NEXT_PAGE_BUTTON = new TranslatedItem(GENERIC_NEXT_PAGE, Material.ARROW);
        BACK_BUTTON = new TranslatedItem(GENERIC_BACK, Material.BOOK);
    }

    private GUISharedObjects() {
    }
}
