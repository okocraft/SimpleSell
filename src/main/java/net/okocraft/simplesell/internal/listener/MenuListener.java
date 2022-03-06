package net.okocraft.simplesell.internal.listener;

import com.google.common.util.concurrent.AtomicDouble;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.okocraft.simplesell.api.SimpleSellApi;
import net.okocraft.simplesell.api.event.ItemSellEvent;
import net.okocraft.simplesell.internal.menu.SellMenu;
import net.okocraft.simplesell.internal.message.CommandMessages;
import net.okocraft.simplesell.internal.util.EconomyHolder;
import net.okocraft.simplesell.internal.util.ItemReturner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MenuListener implements Listener {

    private static final Sound SOUND =
            Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 100f, 1.0f);

    @EventHandler(ignoreCancelled = true)
    public void onOpen(@NotNull InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof SellMenu menu &&
                event.getPlayer() instanceof Player player) {
            menu.setup(player);
            player.playSound(SOUND);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        var inventoryView = event.getView();

        if (inventoryView.getTopInventory().getHolder() instanceof SellMenu menu && event.getSlot() != 49) {
            menu.setShouldUpdate(true);
        }

        var clicked = event.getClickedInventory();

        if (clicked == null || !(clicked.getHolder() instanceof SellMenu menu)) {
            return;
        }

        if (event.getSlot() != 49) {
            return;
        }

        event.setCancelled(true);

        var sellableItems = menu.getSellableItems(true);
        var unsellableItems = menu.getInventory().getContents();
        menu.getInventory().clear();
        player.updateInventory();

        var itemsToSell = new ArrayList<ItemSellEvent.SellItem>(sellableItems.size());

        for (var item : sellableItems) {
            double basePrice = SimpleSellApi.get().getPrice(item.getType());
            itemsToSell.add(new ItemSellEvent.SellItem(item, basePrice));
        }

        List<ItemStack> itemsToReturn = null;

        if (unsellableItems.length != 0) {
            itemsToReturn = new ArrayList<>(Arrays.asList(unsellableItems));
        }

        if (!sellableItems.isEmpty()) {
            var sellEvent = new ItemSellEvent(player, itemsToSell);

            if (sellEvent.callEvent()) {
                var total = sellEvent.calculateTotalPrice();
                EconomyHolder.ECONOMY.depositPlayer(player, total);

                player.sendMessage(
                        CommandMessages.SOLD_MESSAGE.apply(
                                EconomyHolder.ECONOMY.format(total),
                                createReceipt(sellEvent.getSoldItems())
                        )
                );
            } else {
                if (itemsToReturn == null) {
                    itemsToReturn = new ArrayList<>();
                }

                sellEvent.getSoldItems()
                        .stream()
                        .map(ItemSellEvent.SellItem::getItemStack)
                        .forEach(itemsToReturn::add);
            }
        }

        if (itemsToReturn != null && ItemReturner.returnItems(player, itemsToReturn)) {
            player.sendMessage(CommandMessages.RETURN_ITEMS);
        }

        player.closeInventory();
    }

    @EventHandler(ignoreCancelled = true)
    public void onClose(@NotNull InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof SellMenu menu) ||
                !(event.getPlayer() instanceof Player player)) {
            return;
        }

        player.playSound(SOUND);

        menu.cancelUpdateTask();
        menu.getInventory().setItem(49, null);

        if (menu.getInventory().isEmpty()) {
            return;
        }

        if (ItemReturner.returnItems(player, Arrays.asList(menu.getInventory().getContents()))) {
            player.sendMessage(CommandMessages.RETURN_ITEMS);
        }
    }

    private @NotNull Component createReceipt(@NotNull List<ItemSellEvent.SellItem> sellItems) {
        var itemMap = new HashMap<ItemStack, SellResult>(sellItems.size(), 1.0f);

        for (var item : sellItems) {
            var result = itemMap.computeIfAbsent(item.getItemStack(), SellResult::create);
            result.amount.addAndGet(item.getItemStack().getAmount());
            result.totalPrice.addAndGet(item.getBaseItemPrice() * item.getItemStack().getAmount());
        }

        var list = new ArrayList<Component>();

        for (var entry : itemMap.entrySet()) {
            var item = entry.getKey();
            var amount = entry.getValue().amount.get();
            var totalPrice = entry.getValue().totalPrice.get();
            list.add(CommandMessages.RECEIPT_FORMAT.apply(item, amount, EconomyHolder.ECONOMY.format(totalPrice)));
        }

        return Component.join(JoinConfiguration.separator(Component.newline()), list);
    }

    // helper record
    private record SellResult(@NotNull AtomicInteger amount, @NotNull AtomicDouble totalPrice) {

        // helper method
        private static @NotNull SellResult create(@NotNull ItemStack ignored) {
            return new SellResult(new AtomicInteger(), new AtomicDouble());
        }
    }
}
