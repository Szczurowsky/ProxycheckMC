package pl.szczurowsky.mcantiproxy;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.LogPrefix;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import pl.szczurowsky.mcantiproxy.cache.CacheManager;
import pl.szczurowsky.mcantiproxy.commands.AntiProxyCommandsBuilder;
import pl.szczurowsky.mcantiproxy.configs.MessagesConfig;
import pl.szczurowsky.mcantiproxy.configs.PluginConfig;
import pl.szczurowsky.mcantiproxy.listener.PreLoginHandler;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

@Plugin(name = "McAntiProxy-Spigot", version = "1.1")
@Author("Szczurowsky")
@Website("https://szczurowsky.pl")
@Description("🛡️️ Simple plugin which block all player using proxies/VPN basing on proxycheck.io API")
@LogPrefix("McAntiProxy")
public class SpigotPlugin extends JavaPlugin {

    private PluginConfig config;
    private MessagesConfig messagesConfig;
    private LiteCommands<CommandSender> liteCommands;
    private CacheManager cacheManager;

    @Override
    public void onEnable() {
        LocalDateTime starting = LocalDateTime.now();
        Logger logger = this.getLogger();

        logger.info("Enabling anti-proxy plugin");

        registerConfigs();
        logger.info("Configs registered");

        this.cacheManager = new CacheManager(config.getCacheExpirationTime());
        logger.info("Cache manager initialized");

        registerEvents();
        logger.info("Events registered");

        registerCommands();
        logger.info("Commands registered");

        logger.info("Successfully enabled plugin in " + ChronoUnit.MILLIS.between(starting, LocalDateTime.now()) + "ms");
    }

    @Override
    public void onDisable() {
        this.liteCommands.getCommandService().getPlatform().unregisterAll();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PreLoginHandler(config, messagesConfig, cacheManager), this);
    }

    private void registerConfigs() {
        this.config = ConfigManager.create(PluginConfig.class, (config) -> {
            config.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            config.withBindFile(new File(getDataFolder(), "config.yml"));
            config.saveDefaults();
            config.load(true);
        });

        this.messagesConfig = ConfigManager.create(MessagesConfig.class, (config) -> {
            config.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            config.withBindFile(new File(getDataFolder(), "message.yml"));
            config.saveDefaults();
            config.load(true);
        });
    }

    private void registerCommands() {
        this.liteCommands = AntiProxyCommandsBuilder.applyOn(LiteBukkitFactory.builder(getServer(), "antiproxy"), this.config, this.messagesConfig)
                .forwardingRegister();
    }

}
