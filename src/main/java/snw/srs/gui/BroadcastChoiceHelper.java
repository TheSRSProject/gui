package snw.srs.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Material;
import snw.srs.gui.util.TranslatedItem;

import static snw.srs.gui.GUIButtonCallback.clickAndRedraw;
import static snw.srs.gui.i18n.TranslationKeys.PLAYER_SELECTOR_SHOULD_BROADCAST_N;
import static snw.srs.gui.i18n.TranslationKeys.PLAYER_SELECTOR_SHOULD_BROADCAST_Y;

@RequiredArgsConstructor
public final class BroadcastChoiceHelper {
    private static final TranslatedItem NOTICE_TARGET_Y;
    private static final TranslatedItem NOTICE_TARGET_N;
    private final AbstractPluginGUI owner;
    private final int buttonSlot;
    @Getter
    @Setter
    private boolean statusTrue;

    static {
        NOTICE_TARGET_Y = new TranslatedItem(PLAYER_SELECTOR_SHOULD_BROADCAST_Y, Material.GLOWSTONE_DUST);
        NOTICE_TARGET_N = new TranslatedItem(PLAYER_SELECTOR_SHOULD_BROADCAST_N, Material.REDSTONE);
    }

    public void drawButton(GUIButtonHelper helper) {
        final TranslatedItem switchButton = isStatusTrue() ? NOTICE_TARGET_Y : NOTICE_TARGET_N;
        helper.setButton(buttonSlot, owner.buildTranslated(switchButton), clickAndRedraw(owner, (clicker, clickType) -> {
            setStatusTrue(!isStatusTrue());
        }));
    }
}
