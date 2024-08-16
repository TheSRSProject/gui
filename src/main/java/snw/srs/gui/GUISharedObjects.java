package snw.srs.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import snw.srs.gui.util.ItemBuilder;

public final class GUISharedObjects {
    public static final ItemStack FRAME_ITEM;
    public static final ItemStack CANCEL_BUTTON;
    public static final ItemStack PREV_PAGE_BUTTON;
    public static final ItemStack NEXT_PAGE_BUTTON;
    public static final ItemStack BACK_BUTTON;

    static {
        FRAME_ITEM = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("").build();
        CANCEL_BUTTON = new ItemBuilder(Material.BARRIER)
                .setDisplayName("取消")
                .build();
        PREV_PAGE_BUTTON = new ItemBuilder(Material.ARROW)
                .setDisplayName("上一页")
                .build();
        NEXT_PAGE_BUTTON = new ItemBuilder(Material.ARROW)
                .setDisplayName("下一页")
                .build();
        BACK_BUTTON = new ItemBuilder(Material.BOOK)
                .setDisplayName("返回")
                .build();
    }

    private GUISharedObjects() {
    }
}
