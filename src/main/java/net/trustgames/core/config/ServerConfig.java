package net.trustgames.core.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum ServerConfig {
    IP("play.trustgames.net"),
    WEBSITE("discord.trustgames.net"),
    STORE("store.trustgames.net"),
    DISCORD("discord.trustgames.net");

    private final String value;

    ServerConfig(String value) {
        this.value = value;
    }

    /**
     * @return Formatted component text
     */
    public final Component getText() {
        return MiniMessage.miniMessage().deserialize(value);
    }
}
