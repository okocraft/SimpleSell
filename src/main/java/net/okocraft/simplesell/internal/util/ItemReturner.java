package net.okocraft.simplesell.internal.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class ItemReturner {

    public static boolean returnItems(@NotNull Player player, @NotNull List<ItemStack> items) {
        var toReturn = items.stream().filter(Objects::nonNull).toArray(ItemStack[]::new);

        if (toReturn.length == 0) {
            return false;
        } else {
            var toDrop = player.getInventory().addItem(toReturn);
            for (var drop : toDrop.values()) {
                player.getWorld().dropItem(player.getLocation(), drop);
            }
            return true;
        }
    }

    private ItemReturner() {
        throw new UnsupportedOperationException();
    }
}
