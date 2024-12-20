package snw.srs.gui;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import snw.srs.i18n.adventure.bukkit.Player2AudienceMapper;
import snw.srs.i18n.adventure.message.MiniMessageSender;
import snw.srs.i18n.bukkit.BukkitI18NEngine;
import snw.srs.i18n.bukkit.data.PluginJsonTranslationLoader;
import snw.srs.i18n.data.storage.SimpleTranslationStorage;
import snw.srs.i18n.formatter.StringCommonFormatter;
import snw.srs.i18n.message.MessageSender;

import java.util.HashMap;
import java.util.Map;

import static snw.srs.i18n.adventure.bukkit.BukkitAdventure.asBukkitPlayerMessageSender;

final class GUII18NEngineBoard {
    private static final Map<String, BukkitI18NEngine<String>> engines = new HashMap<>();

    private GUII18NEngineBoard() {
    }

    static BukkitI18NEngine<String> getOrCreate(Plugin plugin) {
        return engines.computeIfAbsent(plugin.getName(), unused -> create(plugin));
    }

    private static BukkitI18NEngine<String> create(Plugin plugin) {
        PluginJsonTranslationLoader loader;
        loader = new PluginJsonTranslationLoader(plugin, "gui_lang");
        SimpleTranslationStorage storage = new SimpleTranslationStorage(loader);
        MessageSender<Player, String> messageSender;
        messageSender = asBukkitPlayerMessageSender(MiniMessageSender.INSTANCE, Player2AudienceMapper.create(plugin));
        return new BukkitI18NEngine<>(StringCommonFormatter.INSTANCE, storage, messageSender);
    }

    static void disposeFor(Plugin plugin) {
        engines.remove(plugin.getName());
    }
}
