package org.sausagedev.soseller.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.sausagedev.soseller.SoSeller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&(#\\w{6})");
    private static final FileConfiguration config = Config.getSettings();
    private static final FileConfiguration messages = Config.getMessages();
    private static final SoSeller main = SoSeller.getPlugin();

    public static String getStringByList(List<String> list) {
        StringBuilder stb = new StringBuilder();
        for(String key : list){
            stb.append(key);
            stb.append("\n");
        }
        return convert(stb.toString());
    }


    public static String convert(String msg) {

        msg = ChatColor.translateAlternateColorCodes('&', msg);
        Matcher matcher = HEX_PATTERN.matcher(msg);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, hexToChatColor(hex));
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }


    private static String hexToChatColor(String hex) {
        StringBuilder builder = new StringBuilder("§x");
        for (char c : hex.substring(1).toCharArray()) {
            builder.append('§').append(c);
        }
        return builder.toString();
    }

    public static boolean hasPerm(CommandSender sender, String perm) {
        if (!sender.hasPermission(perm)) {
            String def = "&cУ вас недостаточно прав";
            String msg = messages.getString("have_no_perms", def);
            sender.sendMessage(convert(msg));
            return false;
        }
        return true;
    }

    public static void checkUpdates(SoSeller plugin, Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new URL("https://raw.githubusercontent.com/SausageDeveloper/SoSeller/master/VERSION")
                            .openStream()))) {
                consumer.accept(reader.readLine().trim());
            } catch (IOException ex) {
                plugin.getLogger().warning("Не удалось проверить наличие обновлений: " + ex.getMessage());
            }
        });
    }

    public static void sendMSG(CommandSender p, String path, String def, String arg) {
        String msg = messages.getString(path, def);
        msg = msg.replace("{object}", arg);
        msg = PlaceholderAPI.setPlaceholders((OfflinePlayer) p, msg);
        p.sendMessage(Utils.convert(msg));
    }

    public static void sendMSG(CommandSender p, String path, String def) {
        String msg = messages.getString(path, def);
        msg = PlaceholderAPI.setPlaceholders((OfflinePlayer) p, msg);
        p.sendMessage(Utils.convert(msg));
    }

    public static boolean isNotInt(Object o) {
        try {
            Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return true;
        }
        return false;
    }
    public static boolean isNotDouble(Object o) {
        try {
            Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            return true;
        }
        return false;
    }

    public static void playSound(Player p, String path) {
        String value = config.getString("sounds." + path, "none");
        List<String> params = Arrays.asList(value.split(";"));
        if (value.equalsIgnoreCase("none")) return;
        Sound sound = Sound.valueOf(params.get(0));
        float pitch = Float.parseFloat(params.get(1)) == 0 ? Float.parseFloat(params.get(1)) : 1;
        float volume = Float.parseFloat(params.get(2)) == 0 ? Float.parseFloat(params.get(2)) : 1;

        try {
            p.playSound(p.getLocation(), sound, pitch, volume);
        } catch (IllegalArgumentException e) {
            String msg = "Звук " + sound + " не существует в майнкрафте (Путь: " + "sounds." + path + ")";
            main.getLogger().warning(msg);
        }
    }
}