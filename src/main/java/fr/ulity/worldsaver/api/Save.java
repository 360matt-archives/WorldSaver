package fr.ulity.worldsaver.api;

import fr.ulity.core.api.Config;
import fr.ulity.core.api.Data;
import fr.ulity.worldsaver.WorldSaver;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;



import java.util.*;

public abstract class Save {
    private final World world;
    private World emptyWorld;
    private final Chunk[] chunks;
    private final String filename;

    private Config blockID;


    private final HashMap<String, HashMap<String, String>> changes = new HashMap<>();


    public Save (World world, Chunk[] chunks, String filename) {
        this.world = world;
        this.chunks = chunks;
        this.filename = filename;
        this.totalChunks = chunks.length;
    }

    public void make () {
        pregen(); // generate blocks ID, in the version of server
        copyEmptyWorld(); // create a empty world with the same seed
        loadChunks(); // enable all chunks of Empty world
        store(); // compare world to empty world, and add to HashMap
        save(); // save in file

        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(emptyWorld.getWorldFolder())));

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

    private void loadChunks () {
        Bukkit.getScheduler().scheduleSyncDelayedTask(WorldSaver.plugin, () -> {
            Arrays.stream(emptyWorld.getLoadedChunks()).forEach(Chunk::load);
        }, 20L);
        callback(StatusPassed.LOAD_CHUNKS);
    }

    public int totalChunks = 0;
    public int passedChunks = 0;
    public int progress = 0;

    private void store () {
        for (Chunk ch : chunks) {

            final Chunk al = emptyWorld.getChunkAt(ch.getX(), ch.getZ());

            HashMap<String, String> chunkChanges = new HashMap<>();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {


                    for (int y = 0; y < world.getMaxHeight(); y++) {
                        Material bloc = ch.getBlock(x, y, z).getType();
                        Material defaultBloc = al.getBlock(x, y, z).getType();

                        if (!bloc.equals(defaultBloc)) {
                            Material blocUp = ch.getBlock(x, Math.min(y + 1, world.getMaxHeight()-1), z).getType();

                            int count = 0;
                            final int inittialY = y;

                            while (bloc.equals(blocUp) && y < world.getMaxHeight()) {
                                defaultBloc = al.getBlock(x, y, z).getType();
                                bloc = ch.getBlock(x, y, z).getType();
                                blocUp = ch.getBlock(x, Math.min(y + 1, world.getMaxHeight()-1), z).getType();

                                if (!bloc.equals(defaultBloc)) {
                                    count++;
                                    y++;
                                } else break;
                            }

                            chunkChanges.put(x + "|" + inittialY + "|" + z, blockID.getString(bloc.name()) + "#" + Math.max(1, count));
                        }
                    }

                }
            }

            if (chunkChanges.size() > 0)
                changes.put(ch.getX() + "|" + ch.getZ(), chunkChanges);

            al.unload();
            ch.unload();

            passedChunks++;
            progress = (int) Math.floor(passedChunks/chunks.length)*100;

            callback(StatusPassed.FETCHING);
        }
    }



    public void save () {
        final Data file = new Data(filename, "/addons/WorldSaver/saves");
        file.set("seed", world.getSeed());
        file.set("chunks", changes);
    }




    public enum StatusPassed {BLOCK_ID_INITIALISED, EMPTY_WORLD_CREATED, LOAD_CHUNKS, FETCHING, OPTIMIZE, SAVING }
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
