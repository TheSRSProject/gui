package snw.srs.gui.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class BukkitGenericUtils {
    private BukkitGenericUtils() {
    }

    public static void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 1, 1);
    }
}
