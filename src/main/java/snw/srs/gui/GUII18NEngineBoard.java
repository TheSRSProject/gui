package snw.srs.gui;

import org.bukkit.plugin.Plugin;
import snw.srs.i18n.bukkit.BukkitI18NEngine;
import snw.srs.i18n.bukkit.data.PluginJsonTranslationLoader;
import snw.srs.i18n.bukkit.formatter.ColoredMessageFormatter;
import snw.srs.i18n.data.storage.SimpleTranslationStorage;
import snw.srs.i18n.formatter.StringCommonFormatter;

import java.util.HashMap;
import java.util.Map;

final class GUII18NEngineBoard {
    private static final Map<String, BukkitI18NEngine> engines = new HashMap<>();

    private GUII18NEngineBoard() {
    }

    static BukkitI18NEngine getOrCreate(Plugin plugin) {
        return engines.computeIfAbsent(plugin.getName(), unused -> create(plugin));
    }

    private static BukkitI18NEngine create(Plugin plugin) {
        PluginJsonTranslationLoader loader;
        loader = new PluginJsonTranslationLoader(plugin, "gui_lang");
        SimpleTranslationStorage storage = new SimpleTranslationStorage(loader);
        return new BukkitI18NEngine(new ColoredMessageFormatter(StringCommonFormatter.INSTANCE), storage);
    }

    static void disposeFor(Plugin plugin) {
        engines.remove(plugin.getName());
    }
}
