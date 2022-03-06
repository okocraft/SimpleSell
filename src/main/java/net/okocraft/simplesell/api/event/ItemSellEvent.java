package net.okocraft.simplesell.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ItemSellEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<SellItem> sellItems;
    private boolean cancelled;

    public ItemSellEvent(@NotNull Player player, @NotNull List<SellItem> sellItems) {
        super(player);
        this.sellItems = sellItems;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public @NotNull List<SellItem> getSoldItems() {
        return sellItems;
    }

    public double calculateTotalPrice() {
        double total = 0;

        for (var item : sellItems) {
            total += item.baseItemPrice * item.itemStack.getAmount();
        }

        return total;
    }

    public static final class SellItem {

        private final @NotNull ItemStack itemStack;
        private double baseItemPrice;

        public SellItem(@NotNull ItemStack itemStack, double baseItemPrice) {
            this.itemStack = itemStack;
            this.baseItemPrice = baseItemPrice;
        }

        public @NotNull ItemStack getItemStack() {
            return itemStack;
        }

        public double getBaseItemPrice() {
            return baseItemPrice;
        }

        public void setBaseItemPrice(double baseItemPrice) {
            this.baseItemPrice = baseItemPrice;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (SellItem) obj;
            return Objects.equals(this.itemStack, that.itemStack) &&
                    Double.doubleToLongBits(this.baseItemPrice) == Double.doubleToLongBits(that.baseItemPrice);
        }

        @Override
        public int hashCode() {
            return Objects.hash(itemStack, baseItemPrice);
        }

        @Override
        public String toString() {
            return "SellItem{" +
                    "itemStack=" + itemStack +
                    ", itemPrice=" + baseItemPrice +
                    '}';
        }
    }
}
