package fr.ulity.worldsaver.api;

import de.leonhard.storage.Json;
import fr.ulity.core.api.Api;
import fr.ulity.worldsaver.BlocksEnumsConfigs;
import fr.ulity.worldsaver.WorldSaver;
import fr.ulity.worldsaver.utils.Gzip;
import org.bukkit.*;


import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class Save {
    private final World world;
    private final Chunk[] chunks;
    private final String filename;

    private final HashMap<String, HashMap<String, LinkedList<String>>> changes = new HashMap<>();


    public Save (World world, Chunk[] chunks, String filename) {
        this.world = world;
        this.chunks = chunks;
        this.filename = filename;
        this.totalChunks = chunks.length;
    }

    public void make () {
        Bukkit.getScheduler().runTaskAsynchronously(WorldSaver.plugin, () -> {
            try {
                store(); // compare world to empty world, and add to HashMap
                save(); // save in file
                optimize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public int totalChunks = 0;
    public int passedChunks = 0;
    public int progress = 0;

    private void store () {
        try {
            for (Chunk ch : chunks) {
                HashMap<String, LinkedList<String>> chunkChanges = new HashMap<>();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {

                        LinkedList<String> lister = new LinkedList<>();
                        Material before = Material.AIR;
                        int count = 0;

                        int maxHeight = world.getHighestBlockYAt(x, z);
                        /* for (int loop = world.getMaxHeight()-1; loop > 0; loop--)
                            if (!ch.getBlock(x, loop, z).getType().isAir())
                                maxHeight = loop; */

                        for (int y = 1; y < maxHeight; y++) {
                            Material current = ch.getBlock(x, y, z).getType();

                            if (current.equals(before) && y+1 < maxHeight)
                                count++;
                            else {
                                lister.add(BlocksEnumsConfigs.blocksID.getString(before.name()) + "#" + count);
                                count = 0;
                                before = current;
                            }
                        }

                        if (lister.size() > 0)
                            chunkChanges.put(x + "|" + z, lister);
                    }
                }

                if (chunkChanges.size() > 0)
                    changes.put(ch.getX() + "|" + ch.getZ(), chunkChanges);

                passedChunks++;
                progress = Math.round(passedChunks*100) / totalChunks;

                callback(StatusPassed.FETCHING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void optimize () {

    }


    public void save () {
        try {
            File temp = File.createTempFile("worldsaver_" + filename + "_", ".json");


            File newFile = new File(Api.prefix + "/addons/WorldSaver/saves/" + filename + ".wsaver");
            newFile.getParentFile().mkdir();
            newFile.createNewFile();

            final Json file = new Json(temp);
            file.set("chunks", changes);

            Gzip.compressGZIP(temp, newFile);

            callback(StatusPassed.SAVING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public enum StatusPassed {BLOCK_ID_INITIALISED, EMPTY_WORLD_CREATED, LOAD_CHUNKS, FETCHING, SAVING }
    public abstract void callback (StatusPassed status);

    Date lastCallBack = new Date();
    public boolean cooldown (int seconds) {
        Date now = new Date();
        if (progress == 100 || lastCallBack.getTime() + (seconds*1000) <= now.getTime()) {
            lastCallBack = now;
            return true;
        } else return false;
    }



}
