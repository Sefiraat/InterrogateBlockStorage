package dev.sefiraat.interrogateblockstorage;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@CommandAlias("inter")
public class Commands extends BaseCommand {

    @Default
    public void onDefault(CommandSender sender) {
        if (sender instanceof Player) {
            sender.sendMessage("Please provide a valid subcommand.");
        }
    }

    @Subcommand("dumpAll")
    @CommandPermission("InterrogateBlockStorage.Admin")
    @Description("Creates a file containing a summary of what is in BlockStorage")
    public void dumpAll(CommandSender sender) {

        final Map<String, Integer> map = new LinkedHashMap<>();
        for (World world : Bukkit.getWorlds()) {
            for (Config value : BlockStorage.getRawStorage(world).values()) {
                final String id = value.getString("id");
                map.merge(id, 1, Integer::sum);
            }
        }

        final String path = Interrogate.getInstance().getDataFolder().getAbsolutePath();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/BlockStorageSummary.yml"))) {
            map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(
                entry -> {
                    final String message = entry.getKey() + " -> " + entry.getValue();
                    try {
                        writer.write(message + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sender.sendMessage(message);
                }
            );
            sender.sendMessage("All blocks processed - File saved to: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }




    }
}
