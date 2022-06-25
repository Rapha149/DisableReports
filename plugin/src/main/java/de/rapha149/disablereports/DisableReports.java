package de.rapha149.disablereports;

import de.rapha149.clearfog.version.VersionWrapper;
import de.rapha149.disablereports.Metrics.DrilldownPie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static de.rapha149.disablereports.Messages.getMessage;

public final class DisableReports extends JavaPlugin {

    private static DisableReports instance;
    public VersionWrapper wrapper;

    @Override
    public void onEnable() {
        instance = this;

        String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
        try {
            wrapper = (VersionWrapper) Class.forName(VersionWrapper.class.getPackage().getName() + ".Wrapper" + nmsVersion).getDeclaredConstructor().newInstance();
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
        Metrics metrics = new Metrics(this, 13802);
        metrics.addCustomChart(new DrilldownPie("check_for_updates", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(getDescription().getVersion(), 1);
            map.put(String.valueOf(Config.get().checkForUpdates), entry);
            return map;
        }));
    }
}
