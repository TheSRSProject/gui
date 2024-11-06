package snw.srs.gui.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public final class ItemBuilder {
    private final ItemStack stack;

    public ItemBuilder(Material material) {
        this.stack = new ItemStack(material);
    }

    public ItemBuilder(ItemStack stack) {
        this.stack = stack.clone();
    }

    public ItemBuilder setCount(int count) {
        this.stack.setAmount(count);
        return this;
    }

    public ItemBuilder setDisplayName(String name) {
        return operateMeta(meta -> meta.setDisplayName(ChatColor.RESET + name));
    }

    public ItemBuilder setLore(String... lores) {
        return setLore(Arrays.asList(lores));
    }

    public ItemBuilder setLore(List<String> lores) {
        return operateMeta(meta -> meta.setLore(lores));
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        this.stack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public <T, Z> ItemBuilder setPersistentData(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        return operateMeta(meta -> meta.getPersistentDataContainer().set(key, type, value));
    }

    public ItemStack build() {
        return this.stack.clone();
    }

    public ItemBuilder operateMeta(Consumer<ItemMeta> metaConsumer) {
        final ItemMeta meta = this.stack.getItemMeta();
        metaConsumer.accept(meta);
        this.stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder copy() {
        return new ItemBuilder(this.stack);
    }
}
