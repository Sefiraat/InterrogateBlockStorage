package dev.sefiraat.interrogateblockstorage;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockInfoConfig;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
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

    @Subcommand("checkSpace")
    @CommandPermission("InterrogateBlockStorage.Admin")
    @Description("Creates a file containing a summary of what is in BlockStorage")
    public void checkSpace(CommandSender sender) {
        final Map<String, Double> map = new LinkedHashMap<>();
        for (World world : Bukkit.getWorlds()) {
            final BlockStorage blockStorage = BlockStorage.getStorage(world);
            try {
                final Field inventories = BlockStorage.class.getDeclaredField("inventories");
                inventories.setAccessible(true);
                final Map<Location, BlockMenu> menuMap = (Map<Location, BlockMenu>) inventories.get(blockStorage);
                for (Map.Entry<Location, BlockMenu> entry : menuMap.entrySet()) {
                    final BlockMenu blockMenu = entry.getValue();
                    final Method method = BlockMenu.class.getDeclaredMethod("serializeLocation", Location.class);
                    method.setAccessible(true);
                    final String filePart = (String) method.invoke(null, blockMenu.getLocation());
                    final File file = new File("data-storage/Slimefun/stored-inventories/" + filePart + ".sfi");
                    final long length = file.length();
                    final SlimefunItem item = BlockStorage.check(blockMenu.getBlock());
                    if (item != null) {
                        map.merge(item.getId(), ((length / 1024D) / 1024D), Double::sum);
                    }
                    method.setAccessible(false);
                }
                inventories.setAccessible(false);
            } catch (
                NoSuchFieldException
                | InvocationTargetException
                | NoSuchMethodException
                | IllegalAccessException e
            ) {
                e.printStackTrace();
            }
        }

        final String path = Interrogate.getInstance().getDataFolder().getAbsolutePath();

        final DecimalFormat format = new DecimalFormat("##.##");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/BlockStorageSizeSummary.yml"))) {
            map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(
                entry -> {
                    final String message = entry.getKey() + " -> " + format.format(entry.getValue());
                    try {
                        writer.write(message + "MB\n");
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
