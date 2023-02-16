package net.trustgames.core.config.announcer;

public enum AnnouncerDelayConfig {
    FIRST(30L), // in seconds
    DELAY(360L); // in seconds

    public final long value;

    AnnouncerDelayConfig(long value) {
        this.value = value;
    }
}
