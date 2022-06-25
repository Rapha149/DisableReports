package de.rapha149.disablereports;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Messages {

    private static File messageFile;
    private static FileConfiguration messageConfig;

    static {
        messageFile = new File(DisableReports.getInstance().getDataFolder(), "messages.yml");
        messageConfig = new YamlConfiguration();
        messageConfig.options().copyDefaults(true);
        messageConfig.addDefault("prefix", "&8[&4DisableReports&8] ");
        messageConfig.addDefault("warning", "&e&l[Warning]: &fThis server is configured to remove signatures from incoming messages. " +
                                            "This means having §osecure chat &fenabled can cause problems.");
        messageConfig.addDefault("plugin.enable", "Plugin successfully enabled.");
        messageConfig.addDefault("plugin.disable", "Plugin disabled.");
        messageConfig.addDefault("plugin.up_to_date", "Your version of this plugin is up to date!");
        messageConfig.addDefault("plugin.outdated", "There's a new version available for this plugin: %version%" +
                                                    "\nYou can download it from: %url%");
        messageConfig.addDefault("error", "%prefix%&cAn error occured. Check the console for details.");
        messageConfig.addDefault("syntax", "%prefix%&cSyntax error! Please use &7/%syntax%&c.");
        messageConfig.addDefault("no_permission", "%prefix%&cYou do not have enough permissions to perform this action.");
        messageConfig.addDefault("player_not_found", "%prefix%&cThere's no player with the name &7%name%&c.");
        messageConfig.addDefault("reload", "%prefix%&7Config and messages were reloaded.");
        messageConfig.addDefault("off.not_allowed", "%prefix%&cYou can't turn off disabling reports per command.");
        messageConfig.addDefault("off.turned_on", "%prefix%&7You have turned &aon &7disabling reports for yourself.");
        messageConfig.addDefault("off.turned_off", "%prefix%&7You have turned &4off &7disabling reports for yourself.");
        messageConfig.addDefault("off.others.turned_on", "%prefix%&7You have turned &aon &7disabling reports for &6%player%&7.");
        messageConfig.addDefault("off.others.turned_off", "%prefix%&7You have turned &4off &7disabling reports for &6%player%&7.");
    }

    public static void loadMessages() {
        try {
            if (messageFile.exists())
                messageConfig.load(messageFile);
            else
                messageFile.getParentFile().mkdirs();

            messageConfig.getKeys(true).forEach(key -> {
                if (!messageConfig.getDefaults().isSet(key))
                    messageConfig.set(key, null);
            });

            messageConfig.save(messageFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            DisableReports.getInstance().getLogger().severe("Failed to load message config.");
        }
    }

    public static String getMessage(String key) {
        if (messageConfig.contains(key)) {
            return ChatColor.translateAlternateColorCodes('&', messageConfig.getString(key)
                    .replace("\\n", "\n")
                    .replace("%prefix%", messageConfig.getString("prefix")));
        } else
            throw new IllegalArgumentException("Message key \"" + key + "\" does not exist.");
    }
}