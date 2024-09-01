package fr.Boulldogo.LinkCord.Utils;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import fr.Boulldogo.LinkCord.Main;

public class YamlFileGestionnary {

    private final Main plugin;
    private final File playerFile;
    private YamlConfiguration file;

    public YamlFileGestionnary(Main plugin) {
        this.plugin = plugin;
        this.playerFile = new File(plugin.getDataFolder(), "player-datas.yml");
        checkFile();
        this.file = YamlConfiguration.loadConfiguration(playerFile);
    }

    private void checkFile() {
        if(!playerFile.exists()) {
            try {
                playerFile.createNewFile();
                plugin.getLogger().warning("Player data file not found! The plugin automatically created one!");
            } catch(IOException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Error when trying to create a new player data file! Plugin was disabled!");
                plugin.getPluginLoader().disablePlugin(plugin);
            }
        }
    }

    private void saveFile() {
        try {
            file.save(playerFile);
        } catch(IOException e) {
            plugin.getLogger().severe("Error when trying to save player data file!");
            e.printStackTrace();
        }
    }

    public void registerDataForPlayer(UUID playerUUID, String playerName, String discordTag, String discordAccountId, boolean isBooster) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String basePath = "datas." + playerUUID;

        if(!file.contains(basePath)) {
            file.set(basePath + ".player_name", playerName);
            file.set(basePath + ".discord_tag", discordTag);
            file.set(basePath + ".discord_account_id", discordAccountId);
            file.set(basePath + ".nitro_booster", isBooster);
            file.set(basePath + ".link-time", timestamp);
            saveFile();
        }
    }

    public String getLinkDateForUUID(UUID playerUUID) {
        String path = "datas." + playerUUID + ".link-time";
        if(file.contains(path)) {
            return plugin.getDateByTimestamp(Long.parseLong(file.getString(path)));
        }
        return null;
    }

    public String getDiscordAccountIdByUUID(UUID playerUUID) {
        return file.getString("datas." + playerUUID + ".discord_account_id");
    }

    public String getDiscordTagByUUID(UUID playerUUID) {
        return file.getString("datas." + playerUUID + ".discord_tag");
    }

    public boolean playerExists(UUID playerUUID) {
        return file.contains("datas." + playerUUID);
    }

    public boolean playerIsBooster(UUID playerUUID) {
        return file.getBoolean("datas." + playerUUID + ".nitro_booster", false);
    }

    public boolean discordUUIDExists(String discordId) {
        return file.getValues(false).values().stream()
            .filter(value -> value instanceof YamlConfiguration)
            .map(value ->(YamlConfiguration) value)
            .anyMatch(section -> discordId.equals(section.getString("discord_account_id")));
    }

    public String getPlayernameWithDiscordID(String discordId) {
        return file.getValues(false).values().stream()
            .filter(value -> value instanceof YamlConfiguration)
            .map(value ->(YamlConfiguration) value)
            .filter(section -> discordId.equals(section.getString("discord_account_id")))
            .map(section -> section.getString("player_name"))
            .findFirst().orElse(null);
    }

    public UUID getUUIDByDiscordTag(String discordTag) {
        return file.getKeys(false).stream()
            .filter(key -> discordTag.equals(file.getString("datas." + key + ".discord_tag")))
            .map(UUID::fromString)
            .findFirst().orElse(null);
    }

    public boolean discordAccountIdIsLinked(String discordId) {
        return discordUUIDExists(discordId);
    }

    public void changeDiscordTagForPlayer(UUID playerUUID, String discordTag) {
        String path = "datas." + playerUUID + ".discord_tag";
        if(file.contains(path)) {
            file.set(path, discordTag);
            saveFile();
        }
    }

    public void setPlayerBooster(UUID playerUUID, boolean isBoosting) {
        String path = "datas." + playerUUID + ".nitro_booster";
        if(file.contains(path)) {
            file.set(path, isBoosting);
            saveFile();
        }
    }

    public void removePlayerFromFile(UUID playerUUID) {
        String path = "datas." + playerUUID;
        if(file.contains(path)) {
            file.set(path, null);
            saveFile();
        }
    }

    public boolean playerHasAlreadyLinked(UUID playerUUID) {
        List<String> linkedPlayers = plugin.getConfig().getStringList("link-list");
        return linkedPlayers.contains(playerUUID.toString());
    }

    public void addLinkedPlayer(UUID playerUUID) {
        List<String> linkedPlayers = plugin.getConfig().getStringList("link-list");
        String playerUUIDString = playerUUID.toString();

        if(!linkedPlayers.contains(playerUUIDString)) {
            linkedPlayers.add(playerUUIDString);
            plugin.getConfig().set("link-list", linkedPlayers);
            plugin.saveConfig();
        }
    }
}
