package net.okocraft.simplesell.internal;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.translationloader.directory.TranslationDirectory;
import net.kyori.adventure.key.Key;
import net.milkbowl.vault.economy.Economy;
import net.okocraft.simplesell.api.SimpleSellApi;
import net.okocraft.simplesell.internal.listener.MenuListener;
import net.okocraft.simplesell.internal.menu.SellMenu;
import net.okocraft.simplesell.internal.message.CommandMessages;
import net.okocraft.simplesell.internal.util.EconomyHolder;
import net.okocraft.simplesell.internal.util.ItemReturner;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class SimpleSellPlugin extends JavaPlugin implements SimpleSellApi {

    private final EnumMap<Material, Double> priceMap = new EnumMap<>(Material.class);

    private final TranslationDirectory translationDirectory =
            TranslationDirectory.newBuilder()
                    .setKey(Key.key("simplesell", "languages"))
                    .setDirectory(getDataFolder().toPath().resolve("languages"))
                    .setDefaultLocale(Locale.JAPAN)
                    .onDirectoryCreated(this::saveDefaultLanguages)
                    .build();

    private boolean loadSuccess;

    @Override
    public void onLoad() {
        loadSuccess = reload();
    }

    @Override
    public void onEnable() {
        if (!loadSuccess) {
            getLogger().severe("Failed to load this plugin. Disabling the plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            var economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
            if (economyProvider != null) {
                EconomyHolder.ECONOMY = economyProvider.getProvider();
            }
        } catch (NoClassDefFoundError ignored) {
        }

        if (EconomyHolder.ECONOMY == null) {
            getLogger().severe("Economy not found! Disabling the plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new MenuListener(), this);
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers()
                .forEach(player -> {
                    if (player.getOpenInventory().getTopInventory().getHolder() instanceof SellMenu menu) {
                        var inv = menu.getInventory();
                        inv.setItem(49, null);
                        if (ItemReturner.returnItems(player, Arrays.asList(inv.getContents()))) {
                            player.sendMessage(CommandMessages.RETURN_ITEMS);
                        }
                        player.closeInventory();
                    }
                });

        translationDirectory.unload();
    }

    @Override
    public @NotNull Map<Material, Double> getPriceMap() {
        return priceMap;
    }

    private boolean reload() {
        var config = YamlConfiguration.create(getDataFolder().toPath().resolve("config.yml"));

        try {
            ResourceUtils.copyFromJarIfNotExists(getFile().toPath(), "config.yml", config.getPath());
            config.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load config.yml", e);
            return false;
        }

        try {
            translationDirectory.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load languages", e);
            return false;
        }

        if (!priceMap.isEmpty()) {
            priceMap.clear();
        }

        for (var key : config.getKeyList()) {
            Material material;
            try {
                material = Material.valueOf(key.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Unknown material type: " + key);
                continue;
            }

            double price = config.getDouble(key, 0.0);

            if (price != 0.0) {
                priceMap.put(material, price);
            }
        }

        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("simplesell.command")) {
            sender.sendMessage(CommandMessages.NO_PERMISSION.apply("simplesell.command"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("simplesell.reload")) {
                if (reload()) {
                    sender.sendMessage(CommandMessages.RELOAD_SUCCESS);
                } else {
                    sender.sendMessage(CommandMessages.RELOAD_FAILURE);
                }
            } else {
                sender.sendMessage(CommandMessages.NO_PERMISSION.apply("simplesell.reload"));
            }
            return true;
        }

        if (sender instanceof Player player) {
            SellMenu.open(player);
        } else {
            sender.sendMessage(CommandMessages.ONLY_PLAYER);
        }

        return true;
    }

    private void saveDefaultLanguages(@NotNull Path directory) throws IOException {
        ResourceUtils.copyFromJarIfNotExists(getFile().toPath(), "ja_JP.yml", directory.resolve("ja_JP.yml"));
        //ResourceUtils.copyFromJarIfNotExists(getFile().toPath(), "en.yml", directory.resolve("en.yml"));
    }
}
