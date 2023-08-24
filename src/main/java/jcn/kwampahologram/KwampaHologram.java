package jcn.kwampahologram;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public final class KwampaHologram extends JavaPlugin {
    private final FileConfiguration config = getConfig();
    private final  Logger logger = Bukkit.getLogger();
    private Connection connection;
    private boolean useHolographicDisplays;
    @Override
    public void onEnable() {
        logger.info("Плагин запущен");

        useHolographicDisplays = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");

        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        String host = getConfig().getString("mysql.host");
        int port = getConfig().getInt("mysql.port");
        String database = getConfig().getString("mysql.database");
        String username = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Stats (id INTEGER PRIMARY KEY AUTO_INCREMENT, playername VARCHAR(255), wins BIGINT)");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Hologram();
    }

    @Override
    public void onDisable() {
        logger.info("Плагин отключен");
    }

    public List<String> getTopPlayersByWins(int limit) {
        List<String> topPlayers = new ArrayList<>();

        String query = "SELECT playername, wins FROM Stats ORDER BY wins DESC LIMIT ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, limit);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String playerName = resultSet.getString("playername");
                    int wins = resultSet.getInt("wins");
                    String playerStats = "Ник: " + ChatColor.GOLD + playerName + ChatColor.RESET  + " Победы: " + ChatColor.GOLD +  wins;
                    topPlayers.add(playerStats);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return topPlayers;
    }

    public void Hologram(){
        List<String> list = new ArrayList<>();
        list.add("Топ по победам");
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        Location location = new Location(Bukkit.getWorld("world"), 21, 13, 12);
        Hologram hologram = DHAPI.createHologram("Top", location, list);
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {

            int i = 1;
            @Override
            public void run() {
                System.out.println("Хуй");
                for (String string : getTopPlayersByWins(5)) {
                    DHAPI.setHologramLine(hologram, i, string);
                    i++;
                    if(i == 6   ){
                        i = 1;
                    }
                }
            }
        };
        bukkitRunnable.runTaskTimer(this, 0L, 1200L);

    }
}
