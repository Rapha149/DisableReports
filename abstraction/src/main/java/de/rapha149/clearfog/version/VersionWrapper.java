package de.rapha149.clearfog.version;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public interface VersionWrapper {

    String INCOMING_HANDLER_NAME = "DisableReportsIn";
    String OUTGOING_HANDLER_NAME = "DisableReportsOut";

    void addListener(Player player, Supplier<Boolean> removeSignature, Supplier<Boolean> replaceWithSystemMessage);

    default String adventureToJson(Object obj) {
        if (!(obj instanceof Component component))
            throw new IllegalArgumentException("Object is not of type Component");
        return GsonComponentSerializer.gson().serialize(component);
    }
}
