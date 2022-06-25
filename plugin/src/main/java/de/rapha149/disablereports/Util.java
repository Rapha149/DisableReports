package de.rapha149.disablereports;

import de.rapha149.clearfog.version.VersionWrapper;
import org.bukkit.entity.Player;

public class Util {

    public static VersionWrapper WRAPPER;

    public static boolean shouldDisableReports(Player player) {
        Config config = Config.get();
        switch (config.players.type) {
            case ALL:
                break;
            case OPERATORS:
                if (!player.isOp())
                    return false;
                break;
            case PERMISSION:
                if (!player.hasPermission("disablereports.permission"))
                    return false;
                break;
            case SPECIFIC:
                if (!config.players.specificPlayers.contains(player.getName()) &&
                    !config.players.specificPlayers.contains(player.getUniqueId().toString())) {
                    return false;
                }
                break;
        }

        return !config.turnOff.players.contains(player.getName()) &&
               !config.turnOff.players.contains(player.getUniqueId().toString());
    }
}
