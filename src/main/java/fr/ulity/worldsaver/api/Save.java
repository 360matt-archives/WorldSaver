package fr.ulity.worldsaver.api;

import fr.ulity.core.api.Config;
import fr.ulity.core.api.Data;
import org.bukkit.*;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public abstract class Save {
    private final World world;
    private World emptyWorld;
    private final Chunk[] chunks;
    private final String filename;

    private Config blockID;


    private HashMap<String, HashMap<String, List<String>>> changes = new HashMap<>();


    public Save (World world, Chunk[] chunks, String filename) {
        this.world = world;
        this.chunks = chunks;
        this.filename = filename;
        this.totalChunks = chunks.length;
    }

    public void make () {
        pregen(); // generate blocks ID, in the version of server
        copyEmptyWorld(); // create a empty world with the same seed
        getModifications(); // compare world to empty world


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

    @SuppressWarnings( "deprecation" )
    private void copyEmptyWorld () {
        final String worldname = world.getName() + "_temp_can_be_removed";

        WorldCreator creator = new WorldCreator(worldname);
        creator.seed(world.getSeed());
        creator.type(world.getWorldType());

        emptyWorld = Bukkit.createWorld(creator);
        emptyWorld.setAutoSave(false);

        callback(StatusPassed.EMPTY_WORLD_CREATED);
    }

    public int totalChunks = 0;
    public int passedChunks = 0;
    public int progress = 0;
    private void getModifications () {
        passedChunks = 0;
        for (Chunk ch : chunks) {
            final Chunk al = emptyWorld.getChunkAt(ch.getX(), ch.getZ());
            ch.load();
            al.load();


            HashMap<String, List<String>> chunkChanges = new HashMap<>();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {



                    for (int y = 0; y < world.getMaxHeight(); y++) {
                        List<String> list = new ArrayList<>();

                        Material bloc = ch.getBlock(x, y, z).getType();
                        Material defaultBloc = al.getBlock(x, y, z).getType();
                        if (bloc != defaultBloc) {

                            final int inittialY = y;

                            int count = 0;
                            while (ch.getBlock(x, y, z).getType().equals(ch.getBlock(x, y + 1, z).getType())) {
                                bloc = ch.getBlock(x, y, z).getType();
                                defaultBloc = al.getBlock(x, y, z).getType();
                                if (bloc != defaultBloc && y < world.getMaxHeight()) {
                                    count++;
                                    y++;
                                } else break;
                            }

                            list.add(blockID.getString(bloc.name()) + "#" + Math.max(count, 1));
                            if (list.size() > 0)
                                chunkChanges.put(x + "-" + inittialY + "-" + z, list);
                        }


                    }



                }
            }

            if (chunkChanges.size() > 0)
                changes.put(ch.getX() + "-" + ch.getZ(), chunkChanges);

            al.unload();
            ch.unload();
        }

        final Data file = new Data(filename, "/addons/WorldSaver/saves");
        file.set("seed", world.getSeed());
        file.set("chunks", changes);

        passedChunks++;
        progress = (int) Math.floor(passedChunks/chunks.length)*100;

        callback(StatusPassed.SAVING);
    }




    public enum StatusPassed {BLOCK_ID_INITIALISED, EMPTY_WORLD_CREATED, SAVING }
    public abstract void callback (StatusPassed status);

    Date lastCallBack = new Date();
    public boolean cooldown (int seconds) {
        Date now = new Date();
        if (progress == 100 || lastCallBack.getTime() + seconds*1000 > now.getTime()) {
            lastCallBack = now;
            return true;
        } else return false;
    }



}
