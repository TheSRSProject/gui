package snw.srs.gui.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import snw.srs.i18n.bukkit.BukkitI18NEngine;

import java.util.function.BiFunction;

/**
 * Represents a server-side translated item.
 * Its name depends on the player who will see it.
 */
public record TranslatedItem(
        String nameKey,
        ItemBuilder itemBuilder,
        BiFunction<String, BukkitI18NEngine, String> finalFormatter
) {

    public TranslatedItem(String nameKey, Material material) {
        this(nameKey, new ItemBuilder(material));
    }

    public TranslatedItem(String nameKey, Material material, ChatColor nameColorPrefix) {
        this(nameKey, new ItemBuilder(material), nameColorPrefix);
    }

    public TranslatedItem(String nameKey, ItemBuilder itemBuilder) {
        this(nameKey, itemBuilder, (BiFunction<String, BukkitI18NEngine, String>) null);
    }

    public TranslatedItem(String nameKey, ItemBuilder itemBuilder, ChatColor nameColorPrefix) {
        this(nameKey, itemBuilder, (template, engine) -> nameColorPrefix + template);
    }

    public ItemStack buildFor(Player audience, BukkitI18NEngine i18nEngine) {
        String name = i18nEngine.getTemplateOrAsIs(audience, nameKey);
        String finalName;
        if (finalFormatter != null) {
            finalName = finalFormatter.apply(name, i18nEngine);
        } else {
            finalName = name;
        }
        return itemBuilder.setDisplayName(finalName).build();
    }
}
