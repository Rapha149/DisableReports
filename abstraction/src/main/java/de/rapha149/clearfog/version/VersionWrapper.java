package de.rapha149.clearfog.version;

import org.bukkit.entity.Player;

import java.util.function.Supplier;

public interface VersionWrapper {

    String HANDLER_NAME = "DisableReports";

    void addListener(Player player, Supplier<Boolean> removeSignature);
}
