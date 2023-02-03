package net.trustgames.core.managers;

import net.trustgames.core.Core;
import net.trustgames.core.utils.ColorUtils;
import net.trustgames.core.utils.PlayerUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles cooldowns for chat and commands messages.
 * Can be used by external classes
 */
public class CooldownManager implements Listener {

    private final Core core;

    public CooldownManager(Core core) {
        this.core = core;
    }

    private final HashMap<UUID, Long> commandCooldownTime = new HashMap<>();
    private final HashMap<UUID, Long> cooldownMessageTime = new HashMap<>();

    /**
     * Ensures, that the command is not being spammed too much.
     * There is already one of this check in the CommandManager,
     * but that allows certain number of commands per second.
     * This method allows only one execution of the command per given time.
     * It also ensures that the "don't spam" message is not being sent too often to the player.
     * @param player Player to put in a cooldown
     * @param cooldownTime Cooldown time in seconds
     * @return if the player is on cooldown
     */
    public boolean commandCooldown(Player player, Double cooldownTime){
        UUID uuid = PlayerUtils.getUUID(player);
        /*
         if the player is not in the cooldown yet, or if his cooldown expired,
         put him in the hashmap with the new time
        */
        if (!commandCooldownTime.containsKey(uuid) || !isOnCooldown(player, cooldownTime)) {
            commandCooldownTime.put(uuid, System.currentTimeMillis());
        } else if (isOnCooldown(player, cooldownTime)) {
            sendMessage(player);
            return true;
        }
        return false;
    }

    /**
     * @param player Player to check cooldown on
     * @param cooldownTime Cooldown time in seconds
     * @return if player is on cooldown
     */
    private boolean isOnCooldown(Player player, Double cooldownTime){
        return !(cooldownTime <= (System.currentTimeMillis() - commandCooldownTime.get(PlayerUtils.getUUID(player))) / 1000d);
    }

    /**
     * check if the wait message isn't being sent too often to avoid it being too spammy
     *
     * @param player Player which the cooldown messages are being sent to
     * @return if the cooldown message is too spammy
     */
    private boolean isSpam(Player player) {
        UUID uuid = PlayerUtils.getUUID(player);
        FileConfiguration config = core.getConfig();

        /*
         if he has any last wait message, get the time and make sure the
         current time - the last time of wait message is larger than the min value in config
        */
        if (cooldownMessageTime.containsKey(uuid)) {
            return !(config.getDouble("cooldowns.warn-messages-limit-in-seconds") <= (System.currentTimeMillis() - cooldownMessageTime.get(uuid)) / 1000d);
        } else {
            cooldownMessageTime.put(uuid, System.currentTimeMillis());
            return false;
        }
    }


    /**
     * Send the cooldown messages
     *
     * @param player Player to send the messages to
     */
    private void sendMessage(Player player){
        UUID uuid = PlayerUtils.getUUID(player);
        FileConfiguration config = core.getConfig();

        if (isSpam(player)) return;

        player.sendMessage(ColorUtils.color(Objects.requireNonNull(config.getString("messages.command.spam"))));

        cooldownMessageTime.put(uuid, System.currentTimeMillis());
    }

    // on player quit, remove player's entries from the hashmaps
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        UUID uuid = PlayerUtils.getUUID(player);

        commandCooldownTime.remove(uuid);
        cooldownMessageTime.remove(uuid);
    }
}
