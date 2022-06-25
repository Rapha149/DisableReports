package de.rapha149.disablereports;

import de.rapha149.disablereports.Config.TurnOffData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.rapha149.disablereports.Messages.getMessage;

public class DisableReportsCommand implements CommandExecutor, TabCompleter {

    public DisableReportsCommand(PluginCommand command) {
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if(!sender.hasPermission("disablereports")) {
            sender.sendMessage(getMessage("no_permission"));
            return true;
        }

        if (args.length == 0 || !args[0].toLowerCase().matches("reload|off")) {
            sender.sendMessage(getMessage("syntax").replace("%syntax%", alias + " <reload|off>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if(!sender.hasPermission("disablereports.reload")) {
                    sender.sendMessage(getMessage("no_permission"));
                    return true;
                }

                try {
                    Config.reload();
                    Messages.loadMessages();
                    sender.sendMessage(getMessage("reload"));
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage(getMessage("error"));
                }
                break;
            case "off":
                boolean isPlayer = sender instanceof Player;
                TurnOffData data = Config.get().turnOff;
                if (isPlayer && !data.allowChangePerCommand) {
                    sender.sendMessage(getMessage("off.not_allowed"));
                    break;
                }

                if(!sender.hasPermission("disablereports.off")) {
                    sender.sendMessage(getMessage("no_permission"));
                    return true;
                }

                OfflinePlayer target;
                if (args.length >= 2) {
                    target = Bukkit.getOfflinePlayer(args[1]);
                    if (!target.hasPlayedBefore()) {
                        sender.sendMessage(getMessage("player_not_found").replace("%name%", args[1]));
                        break;
                    }
                } else if (isPlayer) {
                    target = (Player) sender;
                } else {
                    sender.sendMessage(getMessage("syntax").replace("%syntax%", alias + " off <Player>"));
                    break;
                }

                String uuid = target.getUniqueId().toString();
                boolean turnedOn = data.players.remove(uuid) || data.players.remove(target.getName());
                if (!turnedOn)
                    data.players.add(uuid);
                Config.save();

                sender.sendMessage(getMessage("off." + (sender.equals(target) ? "" : "others.") + "turned_" + (turnedOn ? "on" : "off"))
                        .replace("%player%", target.getName()));
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("disablereports"))
            return Collections.emptyList();

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("disablereports.reload"))
                completions.add("reload");
            if (sender.hasPermission("disablereports.off"))
                completions.add("off");
        } else if (args.length == 2 && sender.hasPermission("disablereports.off") &&
                   args[0].equalsIgnoreCase("off")) {
            Bukkit.getOnlinePlayers().stream().map(Player::getName).forEach(completions::add);
        }

        String arg = args[args.length - 1].toLowerCase();
        return completions.stream().filter(s -> s.toLowerCase().startsWith(arg)).toList();
    }
}
