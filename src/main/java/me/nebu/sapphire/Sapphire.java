package me.nebu.sapphire;

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.nebu.sapphire.actions.ActionManager;
import me.nebu.sapphire.chatfilter.ChatFilter;
import me.nebu.sapphire.commands.*;
import me.nebu.sapphire.storage.SQLStorageProvider;
import me.nebu.sapphire.util.Configs;
import me.nebu.sapphire.discord.DiscordBot;
import me.nebu.sapphire.listeners.*;
import me.nebu.sapphire.punishments.PunishmentManager;
import me.nebu.sapphire.storage.StorageProvider;
import me.nebu.sapphire.util.GeneralSettings;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;

public final class Sapphire extends JavaPlugin {

    private static Sapphire instance;
    private static StorageProvider storageProvider;

    @Override
    public void onEnable() {
        Sapphire.instance = this;

//        File configFile = new File(getDataFolder(), "config.yml");
//        if (configFile.exists()) configFile.delete();

        saveDefaultConfig();

        FileConfiguration config = getConfig();

        String configVersion = config.getString("version");

        if (configVersion == null) {
            getLogger().severe("Config version must be defined.");
            disable();
            return;
        }

        if (!getConfig().contains("storage.type")) {
            getLogger().severe("Storage type not found, disabling.");
            disable();
            return;
        }

        switch (getConfig().getString("storage.type")) {
            case "sqlite": {
                try {
                    storageProvider = new SQLStorageProvider(new JdbcConnectionSource("jdbc:sqlite:" + new File(getDataFolder(), "data.db").getAbsolutePath()));
                } catch (SQLException e) {
                    e.printStackTrace();
                    disable();
                }
                break;
            }

            case "mysql": {
                try {
                    if (!config.contains("storage.mysql.username")
                            || !config.contains("storage.mysql.password")
                            || !config.contains("storage.mysql.host")
                            || !config.contains("storage.mysql.port")
                            || !config.contains("storage.mysql.database-name")
                    ) {
                        throw new IllegalStateException("Failed to create a MySQL connection, disabling. Are your connection details correct?");
                    }

                    String username = getConfig().getString("storage.mysql.username");
                    String password = getConfig().getString("storage.mysql.password");
                    String host = getConfig().getString("storage.mysql.host");
                    int port = getConfig().getInt("storage.mysql.port");
                    String databaseName = getConfig().getString("storage.mysql.database-name");

                    HikariConfig hikariConfig = new HikariConfig();
                    hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?useSSL=false&autoReconnect=true&allowPublicKeyRetrieval=true");
                    hikariConfig.setUsername(username);
                    hikariConfig.setPassword(password);
                    hikariConfig.setMaximumPoolSize(10);

                    DataSourceConnectionSource source = new DataSourceConnectionSource(
                            new HikariDataSource(hikariConfig),
                            "jdbc:mysql://" + host + ":" + port + "/" + databaseName
                    );

                    storageProvider = new SQLStorageProvider(source);

                    break;
                } catch (Exception e) {
                    getLogger().severe("Failed to create a MySQL connection, disabling. Are your connection details correct?");
                    e.printStackTrace();
                    disable();
                    return;
                }
            }

            case null:
                getLogger().severe("Failed to detect storage method, disabling.");
                disable();
                return;
            default: {
                getLogger().severe("Failed to detect storage method, disabling.");
                disable();
                return;
            }
        }

        startServices();

        // Register commands
        registerCommands(
                new PunishCommand(),
                new RevertPunishCommand(),
                new HistoryCommand(),
                new ReportCommand(),
                new ReportsCommand(),
                new UpdateReportCommand(),
                new ClaimReportCommand(),
                new SapphireCommand(),
                new StaffChatCommand(),
                new ClearChatCommand(),
                new MuteChatCommand(),
                new VanishCommand(),
                new InventorySeeCommand()
        );

        // Register event listeners
        registerListeners(
                new PunishmentGUIListener(),
                new HistoryGUIListener(),
                new ReportGUIListener(),
                new ReportsGUIListener(),
                new ViewingReportGUIListener(),
                new ChatListener(),
                new LoginListener(),
                new InventorySeeGUIListener(),
                new ConfirmationScreenListener()
        );

        Metrics metrics = new Metrics(this, 31749);
    }

    @Override
    public void onDisable() {
        if (storageProvider != null) storageProvider.shutdown();
        DiscordBot.shutdown();
    }

    public static void disable() {
        Bukkit.getPluginManager().disablePlugin(instance);
    }

    public static Sapphire getInstance() {
        return instance;
    }

    public static StorageProvider getStorageProvider() {
        return storageProvider;
    }

    public static String generateId() {
        byte[] bytes = new byte[4];
        new SecureRandom().nextBytes(bytes);
        return String.format("%08X", new BigInteger(1, bytes));
    }

    public static void startServices() {
        Configs.load();
        GeneralSettings.load();
        Messenger.load();
        PunishmentManager.load();
        ChatFilter.load();
        if (!DiscordBot.isEnabled()) DiscordBot.load();
        ActionManager.load();
    }

    private void registerCommands(Command ... commands) {
        CommandMap map = Bukkit.getCommandMap();

        for (Command cmd : commands) {
            map.getKnownCommands().remove(cmd.getName());
            map.register("sapphire", cmd);
        }
    }

    private static void registerListeners(Listener ... listeners) {
        for (Listener listener : listeners) {
            getInstance().getServer().getPluginManager().registerEvents(listener, getInstance());
        }
    }
}
