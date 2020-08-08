package fr.ulity.worldsaver;

import fr.ulity.core.api.Api;
import fr.ulity.core.api.Initializer;

import org.bukkit.plugin.java.JavaPlugin;
import fr.ulity.worldsaver.commands.DefineCMD;

public final class WorldSaver extends JavaPlugin {
    public static WorldSaver plugin;

    @Override
    public void onEnable() {
        plugin = this;

        Initializer init = new Initializer(this);
        init.requireVersion("2.4");
        if (init.ok) {
            try {
                BlocksEnumsConfigs.generate();
                new DefineCMD(Api.Bukkit.commandMap, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
