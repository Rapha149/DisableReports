package de.rapha149.disablereports;

import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

    private static Map<String, String> comments = new HashMap<>();
    private static Yaml yaml;
    private static DumperOptions options;
    private static File file;
    private static Config config;

    static {
        comments.put("checkForUpdates", "Whether to check for updates on enabling.");
        comments.put("players", "Used to specify the players for whom reports should be disabled.");
        comments.put("players.type", """
                How to choose the players to disable reports for. Possible values:
                - "ALL": Disable reports for all players.
                - "OPERATORS": Only disable reports for operators.
                - "PERMISSION": Only disable reports for players with the permission "disablereports.permission"
                - "SPECIFIC": Only disable reports for the specified players.
                """);
        comments.put("players.specificPlayers", "Only used when \"type\" is \"SPECIFIC\". The players to disable reports for." +
                                        "\nYou can state UUIDs or player names. UUIDs are recommended.");
        comments.put("turnOff", "Used to turn off disabling reports for specific players.");
        comments.put("turnOff.allowChangePerCommand", """
                Whether or not to enable the command "/disablereports off" that players can use to turn off disabling reports for themselves.
                The permission for that command is "disablereports.off" and "disablereports.off.others". If you want all players to be able to use the command, you can use a permission plugin like LuckPerms.
                If you enable this option and the warning message is enabled, it is recommended to include the command in the warning message.
                Warning: when having this option enabled the plugin will edit the config and you will have to pay attention to that when editing the config yourself.""");
        comments.put("turnOff.players", "The players for whom disabling reports should be turned off." +
                                        "\nYou can state UUIDs or player names. UUIDs are recommended.");
        comments.put("warning", """
                Used to control whether or not a warning is sent to players on join.
                This option exists because when players have "secure chat" enabled, it may cause problems when using this plugin.
                You can edit the warning message in the messages.yml file.
                """);
        comments.put("warning.enabled", "Whether or not the message is sent.");
        comments.put("warning.onlyForSpecifiedPlayers", "Whether or not the message should only be sent to players for whom " +
                                                        "reports are actually disabled (see \"players\" and \"turnOff\")." +
                                                        "\nIf disabled, the message will be sent to all players.");
    }

    public static void load() throws IOException {
        options = new DumperOptions();
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setSplitLines(false);
        Representer representer = new Representer();
        representer.setPropertyUtils(new CustomPropertyUtils());
        yaml = new Yaml(new CustomClassLoaderConstructor(DisableReports.getInstance().getClass().getClassLoader()), representer, options);

        file = new File(DisableReports.getInstance().getDataFolder(), "config.yml");
        reload();
    }

    public static void reload() throws IOException {
        if (file.exists())
            config = yaml.loadAs(new FileReader(file), Config.class);
        else {
            file.getParentFile().mkdirs();
            config = new Config();
        }
        save();
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(file)) {
            Pattern pattern = Pattern.compile("([\\s-]*)(\\w+):( .+)?");
            Map<Integer, String> parents = new HashMap<>();
            int lastIndent = 0;
            String[] lines = yaml.dumpAsMap(config).replaceAll("\\[\\n\\s+\\]", "[]").split("\n");
            StringBuilder sb = new StringBuilder("# DisableReports version " + DisableReports.getInstance().getDescription().getVersion() +
                                                 "\n# Github: https://github.com/Rapha149/DisableReports" +
                                                 "\n# Spigot: " + Updates.SPIGOT_URL + "\n");
            for (String line : lines) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    int indent = matcher.group(1).length();
                    parents.put(indent, matcher.group(2));

                    List<String> tree = new ArrayList<>();
                    for (int j = 0; j <= indent; j += options.getIndent())
                        tree.add(parents.get(j));
                    String key = String.join(".", tree);
                    if (comments.containsKey(key)) {
                        if (lastIndent >= indent)
                            sb.append("\n");

                        String prefix = StringUtils.repeat(" ", indent) + "# ";
                        sb.append(prefix + String.join("\n" + prefix, comments.get(key).split("\n")) + "\n" + line + "\n");

                        lastIndent = indent;
                        continue;
                    } else if (matcher.group(3) == null)
                        sb.append("\n");
                    lastIndent = indent;
                }

                sb.append(line + "\n");
            }

            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config get() {
        return config;
    }

    public boolean checkForUpdates = true;
    public PlayersData players = new PlayersData();
    public TurnOffData turnOff = new TurnOffData();
    public WarningData warning = new WarningData();

    public static class PlayersData {

        public PlayersType type = PlayersType.ALL;
        public List<String> specificPlayers = new ArrayList<>();

        public enum PlayersType {
            ALL, OPERATORS, PERMISSION, SPECIFIC
        }
    }

    public static class TurnOffData {

        public boolean allowChangePerCommand = false;
        public List<String> players = new ArrayList<>();
    }

    public static class WarningData {

        public boolean enabled = false;
        public boolean onlyForSpecifiedPlayers = true;
    }
}
