package snw.srs.gui;

import org.bukkit.plugin.Plugin;
import snw.srs.gui.dispatcher.GUIEventDispatcher;

// To make the GUI objects work correctly,
// call the methods below during the lifecycle of your plugin.
@SuppressWarnings("unused")
public final class GUIEntrypoint {
    // just utility methods there so constructor is not necessary to be public
    private GUIEntrypoint() {
    }

    // create GUI objects after calling this or the GUIs won't work correctly
    public static void onEnable(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new GUIEventDispatcher(plugin), plugin);
    }
}
