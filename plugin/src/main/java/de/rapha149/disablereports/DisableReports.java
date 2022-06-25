package de.rapha149.disablereports;

import de.rapha149.clearfog.version.VersionWrapper;
import de.rapha149.disablereports.Config.WarningData;
import de.rapha149.disablereports.Metrics.DrilldownPie;
import de.rapha149.disablereports.Metrics.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static de.rapha149.disablereports.Messages.getMessage;
import static de.rapha149.disablereports.Util.WRAPPER;

public final class DisableReports extends JavaPlugin {

    private static DisableReports instance;

    @Override
    public void onEnable() {
        instance = this;

        String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
        try {
            WRAPPER = (VersionWrapper) Class.forName(VersionWrapper.class.getPackage().getName() + ".Wrapper" + nmsVersion).getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to load support for server version \"" + nmsVersion + "\"");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("DisableReports does not fully support the server version \"" + nmsVersion + "\"");
        }

        Messages.loadMessages();

        try {
            Config.load();
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Failed to load config.");
            getServer().getPluginManager().disablePlugin(this);
        }

        loadMetrics();

        if (Config.get().checkForUpdates) {
            String version = Updates.getAvailableVersion();
            if (version != null) {
                if (version.isEmpty())
                    getLogger().info(getMessage("plugin.up_to_date"));
                else {
                    for (String line : getMessage("plugin.outdated").split("\n"))
                        getLogger().warning(line.replace("%version%", version).replace("%url%", Updates.SPIGOT_URL));
                }
            }
        }

        Bukkit.getOnlinePlayers().forEach(player -> WRAPPER.addListener(player, () -> Util.shouldDisableReports(player)));

        getServer().getPluginManager().registerEvents(new Events(), this);
        new DisableReportsCommand(getCommand("disablereports"));

        getLogger().info(getMessage("plugin.enable"));
    }

    @Override
    public void onDisable() {
        getLogger().info(getMessage("plugin.disable"));
    }

    public static DisableReports getInstance() {
        return instance;
    }

    private void loadMetrics() {
        Metrics metrics = new Metrics(this, 15581);
        metrics.addCustomChart(new DrilldownPie("check_for_updates", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(getDescription().getVersion(), 1);
            map.put(String.valueOf(Config.get().checkForUpdates), entry);
            return map;
        }));
        metrics.addCustomChart(new SimplePie("players_type", () -> {
            char[] arr = Config.get().players.type.toString().toLowerCase().toCharArray();
            arr[0] = Character.toUpperCase(arr[0]);
            return new String(arr);
        }));
        metrics.addCustomChart(new SimplePie("allow_turnoff_change_per_command", () ->
                String.valueOf(Config.get().turnOff.allowChangePerCommand)));
        metrics.addCustomChart(new DrilldownPie("warning", () -> {
            WarningData data = Config.get().warning;
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(String.valueOf(data.onlyForSpecifiedPlayers), 1);
            map.put(String.valueOf(data.enabled), entry);
            return map;
        }));
    }
}
