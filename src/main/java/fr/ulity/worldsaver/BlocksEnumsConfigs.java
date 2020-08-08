package fr.ulity.worldsaver;

import fr.ulity.core.api.Api;
import fr.ulity.core.api.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlocksEnumsConfigs {

    public static Config blocksID;

    public static String getVersion() {
        Pattern pattern = Pattern.compile("(1\\.[0-9]*)\\.?[0-9]*?-");
        Matcher matcher = pattern.matcher(Bukkit.getBukkitVersion());

        return (matcher.find()) ? matcher.group(1): "0";
    }


    private static void developperCreateFile () {
        File temp = new File(Api.prefix + "/addons/WorldSaver/create.yml");

        Config blockID = new Config(temp);
        HashMap<String, Object> defaultContent = new HashMap<>();

        int i = 1;
        for (Material x : Material.values()) {
            if (x.isBlock()) {
                defaultContent.put(String.valueOf(x), String.valueOf(i));
                defaultContent.put(String.valueOf(i), String.valueOf(x));
                i++;
            }
        }
        blockID.putAll(defaultContent);

    }


    public static void generate () throws Exception {

        // developperCreateFile();

        String version = getVersion();
        File file = new File(Api.prefix + "/addons/WorldSaver/blocks_" + version + ".yml");

        blocksID = new Config(file);

        if (!file.exists()) {
            blocksID.addDefaultsFromInputStream(WorldSaver.class.getResourceAsStream("/fr/ulity/worldsaver/referencing/" + version + ".yml"));
        }


    }

}
