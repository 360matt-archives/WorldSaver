package fr.ulity.worldsaver.commands;

import fr.ulity.core.api.CommandManager;
import fr.ulity.worldsaver.api.Restore;
import fr.ulity.worldsaver.exceptions.BackupNotFoundException;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;


public class RestoreCMD extends CommandManager.Assisted {
    public RestoreCMD(CommandMap commandMap, JavaPlugin plugin) {
        super(plugin, "restore");

        registerCommand(commandMap);
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) {
        if (requirePlayer()) {

            World currentWorld = getPlayer().getWorld();

            try {
                new Restore(currentWorld, "Test") {
                    @Override
                    public void callback (StatusPassed status) {
                        if (status.equals(StatusPassed.DECOMPRESSING))
                            sender.sendMessage("§b[WS] §3Décompression du fichier de sauvegarde ...");
                        else if (status.equals(StatusPassed.DECOMPRESSED))
                            sender.sendMessage("§b[WS] §3Fichier décompressé ...");
                        else if (status.equals(StatusPassed.RESTORE) && cooldown(3)) {
                            sender.sendMessage(
                                    "§b[WS] §3Progression: §c%progress%% §3: (§b%passedChunks% §3/ §b%totalChunks% chunks §3restaurés)"
                                            .replaceAll("%progress%", String.valueOf(progress))
                                            .replaceAll("%passedChunks%", String.valueOf(passedChunks))
                                            .replaceAll("%totalChunks%", String.valueOf(totalChunks))
                            );
                        }

                    }
                }.restoreAll();
            } catch (BackupNotFoundException e) {
                e.printStackTrace();
            }


        }
    }
}
