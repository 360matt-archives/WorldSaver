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
            new Save(currentWorld, new Chunk[]{}, "Test") {
                @Override
                public void callback(StatusPassed name) {
                    Bukkit.broadcastMessage("Voila");
                }
            }.make();










        }
    }
}
