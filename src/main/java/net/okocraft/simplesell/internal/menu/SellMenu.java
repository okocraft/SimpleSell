package net.okocraft.simplesell.internal.menu;

import com.google.common.util.concurrent.AtomicDouble;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.okocraft.simplesell.api.SimpleSellApi;
import net.okocraft.simplesell.internal.SimpleSellPlugin;
import net.okocraft.simplesell.internal.message.CommandMessages;
import net.okocraft.simplesell.internal.util.EconomyHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SellMenu implements InventoryHolder {

    public static void open(@NotNull Player player) {
        player.openInventory(new SellMenu().inventory);
    }

    private final Inventory inventory;
    private BukkitTask updateTask;

    private boolean shouldUpdate = false;

    private SellMenu() {
        this.inventory = Bukkit.createInventory(this, 6 * 9, CommandMessages.MENU_TITLE);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setup(@NotNull Player player) {
        var button = new ItemStack(Material.SUNFLOWER);

        button.editMeta(meta -> meta.displayName(GlobalTranslator.render(
                CommandMessages.SELL_BUTTON_DISPLAY_NAME.apply(EconomyHolder.ECONOMY.format(0)),
                player.locale()
        )));

        inventory.setItem(49, button);

        updateTask = Bukkit.getScheduler().runTaskTimer(
                JavaPlugin.getPlugin(SimpleSellPlugin.class),
                () -> {
                    if (shouldUpdate) {
                        shouldUpdate = false;
                        updateSellButton(player.locale());
                        player.updateInventory();
                    }
                },
                20, 20
        );
    }

    public @NotNull List<ItemStack> getSellableItems(boolean removeFromInventory) {
        var list = new ArrayList<ItemStack>();

        var items = inventory.getContents();

        for (int i = 0; i < items.length; i++) {
            if (i == 49) {
                continue;
            }

            var item = items[i];

            if (item != null && SimpleSellApi.get().isSellable(item)) {
                list.add(item);

                if (removeFromInventory) {
                    items[i] = null;
                }
            }
        }

        if (removeFromInventory) {
            items[49] = null; // remove button on sell
            inventory.setContents(items);
        }

        return list;
    }

    public void setShouldUpdate(boolean shouldUpdate) {
        this.shouldUpdate = shouldUpdate;
    }

    public void cancelUpdateTask() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
    }

    private void updateSellButton(@NotNull Locale locale) {
        var button = new ItemStack(Material.SUNFLOWER);

        button.editMeta(meta -> {
            var totalPrice = new AtomicDouble();
            var lore = new ArrayList<Component>();

            getSellableItems(false).stream()
                    .collect(Collectors.groupingBy(ItemStack::getType, Collectors.summingInt(ItemStack::getAmount)))
                    .forEach((item, amount) -> {
                        double price = SimpleSellApi.get().getPrice(item) * amount;
                        totalPrice.addAndGet(price);
                        lore.add(GlobalTranslator.render(
                                CommandMessages.SELL_BUTTON_FORMAT.apply(item, amount, EconomyHolder.ECONOMY.format(price)),
                                locale
                        ));
                    });

            meta.displayName(GlobalTranslator.render(
                    CommandMessages.SELL_BUTTON_DISPLAY_NAME.apply(EconomyHolder.ECONOMY.format(totalPrice.get())),
                    locale
            ));

            meta.lore(lore);
        });

        inventory.setItem(49, button);
    }
}
