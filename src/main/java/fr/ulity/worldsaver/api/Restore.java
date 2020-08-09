package fr.ulity.worldsaver.api;

import de.leonhard.storage.Json;
import fr.ulity.core.api.Api;
import fr.ulity.worldsaver.BlocksEnumsConfigs;
import fr.ulity.worldsaver.WorldSaver;
import fr.ulity.worldsaver.exceptions.BackupNotFoundException;
import fr.ulity.worldsaver.utils.Gzip;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Restore {
    private final World world;
    private final String filename;
    private final File file;

    public Restore (World world, String filename) throws BackupNotFoundException {
        this.world = world;
        this.filename = filename;

        this.file = new File(Api.prefix + "/addons/WorldSaver/saves/" + filename + ".wsaver");
        if (!this.file.exists())
            throw new BackupNotFoundException(filename);

    }


    Pattern patternChunk = Pattern.compile("^(-?[0-9]*)\\|(-?[0-9]*)$");
    Pattern patternRelative = Pattern.compile("^([0-9]*)\\|([0-9]*)$");
    Pattern patternElement = Pattern.compile("^([0-9]*)#([0-9]*)$");


    public void restoreAll () { restore(true, new Chunk[0]); }
    public void restoreChunks (Chunk[] chunks) { restore(false, chunks); }


    public int totalChunks = 0;
    public int passedChunks = 0;
    public int progress;
    private void restore (boolean all, Chunk[] chunks)  {

        Bukkit.getScheduler().scheduleSyncDelayedTask(WorldSaver.plugin, () -> {
            List<Chunk> chunksPermitted = Arrays.asList(chunks);

            try {
                File json = File.createTempFile("ws_", ".json");
                callback(StatusPassed.DECOMPRESSING);
                Gzip.decompressGzip(file, json);
                callback(StatusPassed.DECOMPRESSED);

                Json data = new Json(json);


                totalChunks = data.singleLayerKeySet().size();

                for (String ch : data.singleLayerKeySet()) {
                    if (stopped)
                        return;

                    Matcher matcherChunk = patternChunk.matcher(ch);
                    if (matcherChunk.find()) {
                        int xChunk = Integer.parseInt(matcherChunk.group(1));
                        int zChunk = Integer.parseInt(matcherChunk.group(2));
                        Chunk chunk = world.getChunkAt(xChunk, zChunk);

                        if (!all && !chunksPermitted.contains(chunk))
                            continue; // next chunk, ignore the suite code for this chunk


                        for (Map.Entry<String, List<String>> pos : ((HashMap<String, List<String>>) data.get(ch)).entrySet()) {
                            Matcher matcherPos = patternRelative.matcher(pos.getKey());
                            if (matcherPos.find()) {
                                int xPos = Integer.parseInt(matcherPos.group(1));
                                int zPos = Integer.parseInt(matcherPos.group(2));

                                int count = 0;
                                for (String x : pos.getValue()) {
                                    /* ex value:
                                       10#3
                                    */


                                    // loop all content of ONE verticale blocks
                                    Matcher matcherElement = patternElement.matcher(x);

                                    if (matcherElement.find()) {
                                       for (int k = 0; k <= Integer.parseInt(matcherElement.group(2)); k++) {
                                            // looping verticale optimization to the top

                                           Material material = Material.valueOf(BlocksEnumsConfigs.blocksID.getString(matcherElement.group(1)));
                                           chunk.getBlock(xPos, count, zPos).setType(material);

                                           count++;
                                        }
                                    }
                                }

                                for (int k = count; k < world.getMaxHeight(); k++)
                                    chunk.getBlock(xPos, k, zPos).setType(Material.AIR);
                                    // replace leaving air
                            }
                        }
                    }

                    passedChunks++;
                    progress = Math.round(passedChunks * 100) / totalChunks;
                    callback(StatusPassed.RESTORE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }


    public boolean stopped = false;
    public void stop () {
        stopped = true;
        callback(Restore.StatusPassed.STOPPED);
    }


    public enum StatusPassed { DECOMPRESSING, DECOMPRESSED, RESTORE, STOPPED }
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
