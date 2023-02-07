package net.trustgames.core.database.player_activity;

import net.trustgames.core.Core;
import net.trustgames.core.cache.EntityCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
This class is used for listeners for table player_activity
 */
public class ActivityListener implements Listener {

    private final Core core;

    public ActivityListener(Core core) {
        this.core = core;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = EntityCache.getUUID(event.getPlayer());

        writeActivity(uuid, "JOIN SERVER " + Bukkit.getServer().getName() + " (" + Bukkit.getServer().getPort() + ")", true);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = EntityCache.getUUID(event.getPlayer());

        writeActivity(uuid, "QUIT SERVER " + Bukkit.getServer().getName() + " (" + Bukkit.getServer().getPort() + ")", true);
    }

    /**
     * Used on a server shutdown, as the player quit event is not executed on a server shutdown.
     * It writes the activity to the database of the player's quit with the reason of server
     * shutdown. It isn't run async, unlike the others, because on shutdown, it wouldn't work.
     *
     * @param uuid UUID of Player to write activity to
     */
    public void onServerShutdown(UUID uuid) {
        writeActivity(uuid, "QUIT SHUTDOWN SERVER " + Bukkit.getServer().getName() + " (" + Bukkit.getServer().getPort() + ")", false);
    }

    /**
    Gets the player's last activity by using external method from PlayerActivityDB
    to find player's last activity by his uuid. If the activity is null, meaning the player
    probably doesn't have any activities saved in the table yet, it creates one with specified values
     * @param uuid UUID of Player to write activity to
     * @param runAsync Should the method be run Async
     * @return Player Activity data
     */
    private PlayerActivity getPlayerActivityFromDatabase(UUID uuid, boolean runAsync) {
        PlayerActivityDB playerActivityDB = new PlayerActivityDB(core);
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) return null;

        PlayerActivity playerActivity = playerActivityDB.findPlayerActivityByUUID(uuid.toString());

        if (playerActivity == null) {
            playerActivity = new PlayerActivity(uuid.toString(), Objects.requireNonNull(
                    player.getAddress(), "Player " + uuid + " IP address is null!")
                    .getHostString(), "FIRST JOIN PORT " + Bukkit.getServer().getPort(),
                    new Timestamp(Instant.now().toEpochMilli()));

            playerActivityDB.createPlayerActivity(playerActivity, runAsync);
            return null;
        } else {
            return playerActivity;
        }
    }

    /**
    writes the values for the newly created player activity to the table.
    Assigns a new player activity by getting the player's activity from
    the database and setting the values accordingly. Then it creates the full row.
     * @param uuid UUID of Player to write activity to
     * @param action What actions to write
     * @param runAsync Should the method be run Async
     */
    private void writeActivity(UUID uuid, String action, boolean runAsync) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) return;
        if (core.getMariaDB().isMySQLDisabled()) return;

        PlayerActivity playerActivity;
        playerActivity = getPlayerActivityFromDatabase(uuid, runAsync);
        if (playerActivity != null) {
            PlayerActivityDB playerActivityDB = new PlayerActivityDB(core);

            playerActivity.setIp(Objects.requireNonNull(player.getAddress()).getHostString());

            playerActivity.setAction(action);
            playerActivity.setTime(new Timestamp(Instant.now().toEpochMilli()));

            playerActivityDB.createPlayerActivity(playerActivity, runAsync);
        }
    }
}
