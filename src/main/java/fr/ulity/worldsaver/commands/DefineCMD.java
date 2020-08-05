package fr.ulity.worldsaver.commands;

import fr.ulity.core.api.CommandManager;
import fr.ulity.worldsaver.api.Save;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class DefineCMD extends CommandManager.Assisted {
    public DefineCMD(CommandMap commandMap, JavaPlugin plugin) {
        super(plugin, "gen");

        registerCommand(commandMap);
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) {
        if (requirePlayer()) {

            World currentWorld = getPlayer().getWorld();

            new Save(currentWorld, new Chunk[]{getPlayer().getLocation().getChunk()}, "Test") {
                @Override
                public void callback (StatusPassed name) {
                    if (name.equals(StatusPassed.BLOCK_ID_INITIALISED)) {
                        sender.sendMessage("§b[WS] §3Initialisation des ID ...");
                    } else if (name.equals(StatusPassed.EMPTY_WORLD_CREATED)) {
                        sender.sendMessage("§b[WS] §3Récupération de la map par défaut ...");
                        sender.sendMessage("§b[WS] §3/!\\ Cela peut prendre un instant ...");
                    } else if (name.equals(StatusPassed.LOAD_CHUNKS)) {
                        sender.sendMessage("§b[WS] §3/!\\ Réveil des chunks ...");
                    } else if (name.equals(StatusPassed.SAVING) && cooldown(3)) {
                        sender.sendMessage(
                                "§b[WS] §3Progression: §c%progress%% §3: (§b%passedChunks% §3/ §b%totalChunks% chunks §3analysés)"
                                .replaceAll("%progress%", String.valueOf(progress))
                                .replaceAll("%passedChunks%", String.valueOf(passedChunks))
                                .replaceAll("%totalChunks%", String.valueOf(totalChunks))
                        );
                    }
                }
            }.make();



        }
    }
}
