package net.trustgames.core.player.data;

import net.kyori.adventure.text.Component;
import net.trustgames.core.player.data.config.PlayerDataType;
import net.trustgames.core.utils.MiniMessageUtils;

/**
 * All configurable messages for PlayerDataCommand
 */
public enum PlayerDataConfig {
    PREFIX("<color:#3498db>Economy | </color>"),
    SET(PREFIX.message + "<dark_gray>You have set <yellow><value> <player_data><dark_gray> to <white><player_name>"),
    ADD(PREFIX.message + "<dark_gray>You have added <yellow><value> <player_data><dark_gray> to <white><player_name>"),
    REMOVE(PREFIX.message + "<dark_gray>You have removed <yellow><value> <player_data><dark_gray> from <white><player_name>"),
    GET_OTHER(PREFIX.message + "<dark_gray><white><player_name><dark_gray> has <yellow><value> <player_data>"),
    GET_PERSONAL(PREFIX.message + "<dark_gray>You have <yellow><value> <player_data>"),
    INVALID_TYPE(PREFIX.message + "<red>Invalid data type!"),
    INVALID_ACTION(PREFIX.message + "<red>Invalid action for <white><player_data><red>!"),
    INVALID_VALUE(PREFIX.message + "<red>Invalid value <white><value><red>!");

    private final String message;

    PlayerDataConfig(String message) {
        this.message = message;
    }

    /**
     * Replace player data tags with player data
     *
     * @param playerName Name of the Player to replace the tags with value of
     * @return New formatted Component message with replaced tags
     */
    public final Component formatMessage(String playerName, PlayerDataType dataType, String value) {
        return MiniMessageUtils.playerData(playerName, dataType, value).deserialize(message);
    }
}
