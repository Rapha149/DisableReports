package de.rapha149.disablereports;

import de.rapha149.disablereports.Config.WarningData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static de.rapha149.disablereports.Messages.getMessage;
import static de.rapha149.disablereports.Util.WRAPPER;

public class Events implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        WRAPPER.addListener(player,
                () -> Util.shouldDisableReports(player),
                () -> Config.get().replaceWithSystemMessage);

        WarningData data = Config.get().warning;
        if (data.enabled && (!data.onlyForSpecifiedPlayers || Util.shouldDisableReports(player)))
            player.sendMessage(getMessage("warning"));
    }
}
