package fr.ulity.worldsaver.api;

import fr.ulity.core.api.Config;
import org.bukkit.*;

import java.io.File;
import java.util.HashMap;

public abstract class Save {
    private final World world;
    private World emptyWorld;
    private final Chunk[] chunks;
    private final String filename;

    private Config blockID;

    public Save (World world, Chunk[] chunks, String filename) {
        this.world = world;
        this.chunks = chunks;
        this.filename = filename;
    }

    public void make () {
        pregen(); // generate blocks ID, in the version of server
        copyEmptyWorld(); // create a empty world with the same seed


    }


    private void pregen () {
        blockID = new Config("IDs", "addons/WorldSaver");
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
        callback(StatusPassed.BLOCK_ID_INITIALISED);
    }

    private void copyEmptyWorld () {
        String worldname = world.getName() + "_temp_can_be_removed";

        emptyWorld = Bukkit.getWorld(worldname);
        if (emptyWorld != null) {
            Bukkit.unloadWorld(emptyWorld, false);
            new File(worldname).delete();
        }

        WorldCreator creator = new WorldCreator(worldname);
        creator.seed(world.getSeed());

        emptyWorld = Bukkit.createWorld(creator);
        callback(StatusPassed.EMPTY_WORLD_CREATED);
    }




    public enum StatusPassed {BLOCK_ID_INITIALISED, EMPTY_WORLD_CREATED }
    public abstract void callback (StatusPassed status);

}
