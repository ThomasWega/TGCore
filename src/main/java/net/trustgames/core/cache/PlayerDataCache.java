package net.trustgames.core.cache;

import net.trustgames.core.Core;
import net.trustgames.core.player.data.PlayerDataFetcher;
import net.trustgames.core.player.data.config.PlayerDataIntervalConfig;
import net.trustgames.core.player.data.config.PlayerDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Get or update player's data in the redis cache
 */
public final class PlayerDataCache {

    private final Core core;
    private final JedisPool pool;
    private final UUID uuid;
    private final PlayerDataType dataType;


    public PlayerDataCache(Core core,
                           @NotNull UUID uuid,
                           @NotNull PlayerDataType dataType) {
        this.core = core;
        this.uuid = uuid;
        this.pool = core.getJedisPool();
        this.dataType = dataType;
    }

    /**
     * Update the specified data of player in the
     * redis cache with the given value
     *
     * @param value Value to update the data with
     */
    public void update(@NotNull String value) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Jedis jedis = pool.getResource()) {
                String column = dataType.getColumnName();
                jedis.hset(uuid.toString(), column, value);
            }
        });
    }

    /**
     * Gets the specified value of data from the cache.
     * The cache should always be up-to-date with the database.
     *
     * @param callback Callback where the result is saved
     */
    public void get(Consumer<@Nullable String> callback) {
        core.getServer().getScheduler().runTaskAsynchronously(core, () -> {
            try (Jedis jedis = pool.getResource()) {
                String result = jedis.hget(uuid.toString(), dataType.getColumnName());
                jedis.expire(uuid.toString(), PlayerDataIntervalConfig.DATA_EXPIRY.getSeconds());
                if (result == null) {
                    PlayerDataFetcher dataFetcher = new PlayerDataFetcher(core, dataType);
                    dataFetcher.fetch(uuid, data -> {
                        // if still null, there is no data on the player even in the database
                        if (data != null)
                            update(data);
                        callback.accept(data);
                    });
                    return;
                }
                callback.accept(result);
            }
        });
    }
}
