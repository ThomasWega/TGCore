package net.trustgames.core.config.database.player_data;

/**
 * Stores all the PlayerData types and their column names
 * which are saved in the player data table.
 */
public enum PlayerDataType {
    UUID("uuid", "VARCHAR(36) primary key"),
    KILLS("kills", "INT signed DEFAULT 0"),
    DEATHS("deaths", "INT DEFAULT 0"),
    GAMES("games_played", "INT DEFAULT 0"),
    PLAYTIME("playtime", "INT DEFAULT 0"),
    XP("xp", "INT DEFAULT 0"),
    LEVEL("", ""),
    GEMS("gems", "INT DEFAULT 100"),
    RUBIES("rubies", "INT DEFAULT 0");

    private final String columnName;
    private final String columnType;

    PlayerDataType(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public final String getColumnName() {
        return columnName;
    }

    public final String getColumnType() {
        return columnType;
    }
}
