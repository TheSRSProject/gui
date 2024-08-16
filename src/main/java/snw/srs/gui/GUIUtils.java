package snw.srs.gui;

import org.bukkit.plugin.Plugin;

import java.util.concurrent.Executor;

public final class GUIUtils {
    private GUIUtils() {
    }

    public static Executor runTask(Plugin plugin) {
        return runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable);
    }
}
