package net.okocraft.simplesell.internal.message;

import com.github.siroshun09.translationloader.argument.DoubleArgument;
import com.github.siroshun09.translationloader.argument.SingleArgument;
import com.github.siroshun09.translationloader.argument.TripleArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

public final class CommandMessages {

    private static final Style NOT_ITALIC = Style.style(TextDecoration.ITALIC.withState(TextDecoration.State.FALSE));

    public static final SingleArgument<String> NO_PERMISSION =
            permissionNode ->
                    Component.translatable()
                            .key("simplesell.message.no-permission")
                            .args(Component.text(permissionNode, AQUA))
                            .color(RED)
                            .build();

    public static final Component ONLY_PLAYER = Component.translatable("simplesell.message.only-player", RED);

    public static final Component RELOAD_SUCCESS = Component.translatable("simplesell.message.reload-success", GRAY);

    public static final Component RELOAD_FAILURE = Component.translatable("simplesell.message.reload-failure", GRAY);

    public static final DoubleArgument<String, Component> SOLD_MESSAGE =
            (totalPrice, receipt) ->
                    Component.translatable()
                            .key("simplesell.message.sold")
                            .args(
                                    Component.text(totalPrice, AQUA),
                                    CommandMessages.HOVER_TO_SHOW_RECEIPT.hoverEvent(HoverEvent.showText(receipt))
                            )
                            .color(GRAY)
                            .build();

    private static final Component HOVER_TO_SHOW_RECEIPT =
            Component.translatable("simplesell.message.hover-to-show-receipt", YELLOW);

    public static final TripleArgument<ItemStack, Integer, String> RECEIPT_FORMAT =
            (item, amount, totalPrice) ->
                    Component.translatable()
                            .key("simplesell.message.receipt-format")
                            .args(
                                    Component.translatable(item, NamedTextColor.AQUA),
                                    Component.text(amount, NamedTextColor.AQUA),
                                    Component.text(totalPrice, NamedTextColor.YELLOW)
                            )
                            .color(GRAY)
                            .build();

    public static final Component RETURN_ITEMS =
            Component.translatable("simplesell.message.return-items", GRAY);

    public static final Component MENU_TITLE = Component.translatable("simplesell.menu.title");

    public static final SingleArgument<String> SELL_BUTTON_DISPLAY_NAME =
            totalPrice ->
                    Component.translatable()
                            .key("simplesell.menu.sell-button.display-name")
                            .args(Component.text(totalPrice, AQUA))
                            .style(NOT_ITALIC.color(GOLD))
                            .build();

    public static final TripleArgument<Material, Integer, String> SELL_BUTTON_FORMAT =
            (item, amount, totalPrice) ->
                    Component.translatable()
                            .key("simplesell.menu.sell-button.lore-format")
                            .args(
                                    Component.translatable(item, NamedTextColor.AQUA),
                                    Component.text(amount, NamedTextColor.AQUA),
                                    Component.text(totalPrice, NamedTextColor.YELLOW)
                            )
                            .style(NOT_ITALIC.color(GRAY))
                            .build();

    private CommandMessages() {
        throw new UnsupportedOperationException();
    }
}
