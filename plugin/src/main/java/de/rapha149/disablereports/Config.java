package de.rapha149.disablereports;

import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Config {

    private static Map<String, String> comments = new HashMap<>();
    private static Config config;

    static {
        comments.put("checkForUpdates", "Whether to check for updates on enabling.");
    }

    public static void load() throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setSplitLines(false);
        Representer representer = new Representer();
        representer.setPropertyUtils(new CustomPropertyUtils());
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(DisableReports.getInstance().getClass().getClassLoader()), representer, options);

        File file = new File(DisableReports.getInstance().getDataFolder(), "config.yml");
        if (file.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String content = br.lines().collect(Collectors.joining("\n"));
            br.close();

            String line = content.split("\n")[0];
            Matcher matcher = Pattern.compile("# VoidTotem version ((\\d|\\.)+)").matcher(line);
            if(matcher.matches()) {
                String version = matcher.group(1);
                if(Updates.compare(version, "1.3.4") <= 0)
                    content = content.replaceFirst("advancement: (true|false)", "advancement: {}");
            }

            config = yaml.loadAs(content, Config.class);
        } else {
            file.getParentFile().mkdirs();
            config = new Config();
        }

        try (FileWriter writer = new FileWriter(file)) {
            Pattern pattern = Pattern.compile("((\\s|-)*)(\\w+):( .+)?");
            Map<Integer, String> parents = new HashMap<>();
            int lastIndent = 0;
            String[] lines = yaml.dumpAsMap(config).split("\n");
            StringBuilder sb = new StringBuilder("# DisableReports version " + DisableReports.getInstance().getDescription().getVersion() +
                                                 "\n# Github: https://github.com/Rapha149/DisableReports" +
                                                 "\n# Spigot: " + Updates.SPIGOT_URL + "\n");
            for (String line : lines) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    int indent = matcher.group(1).length();
                    parents.put(indent, matcher.group(3));

                    List<String> tree = new ArrayList<>();
                    for (int j = 0; j <= indent; j += options.getIndent())
                        tree.add(parents.get(j));
                    String key = String.join(".", tree);
                    if (comments.containsKey(key)) {
                        if (lastIndent == indent)
                            sb.append("\n");

                        String prefix = StringUtils.repeat(" ", indent) + "# ";
                        sb.append(prefix + String.join("\n" + prefix, comments.get(key).split("\n")) + "\n" + line + "\n");

                        lastIndent = indent;
                        continue;
                    } else if (matcher.group(4) == null)
                        sb.append("\n");
                    lastIndent = indent;
                }

                sb.append(line + "\n");
            }

            writer.write(sb.toString().replaceAll("\\[\\n\\s+\\]", "[]"));
        }
    }

    public static Config get() {
        return config;
    }

    public boolean checkForUpdates = true;
}
