package snw.srs.gui;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.function.BiConsumer;

import static snw.srs.gui.util.BukkitGenericUtils.playSound;

public interface GUIButtonCallback {
    GUIClickResult handle(Player clicker, ClickType clickType);

    static GUIButtonCallback clickAndRedraw(AbstractPluginGUI self, BiConsumer<Player, ClickType> handler) {
        return (clicker, clickType) -> {
            handler.accept(clicker, clickType);
            playSound(clicker, Sound.UI_BUTTON_CLICK);
            self.scheduleDraw();
            return GUIClickResult.CANCEL_CLICK;
        };
    }
}
