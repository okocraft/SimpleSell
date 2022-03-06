package net.okocraft.simplesell.api;

import net.okocraft.simplesell.internal.SimpleSellPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface SimpleSellApi {

    static @NotNull SimpleSellApi get() {
        return JavaPlugin.getPlugin(SimpleSellPlugin.class);
    }

    @NotNull Map<Material, Double> getPriceMap();

    default boolean isSellable(@NotNull Material material) {
        return getPriceMap().containsKey(material);
    }

    default boolean isSellable(@NotNull ItemStack item) {
        return !item.hasItemMeta() && getPriceMap().containsKey(item.getType());
    }

    default double getPrice(@NotNull Material material) {
        return getPriceMap().getOrDefault(material, 0.0);
    }

    default double calculatePrice(@NotNull ItemStack itemStack) {
        return isSellable(itemStack) ? getPrice(itemStack.getType()) * itemStack.getAmount() : 0;
    }
}
