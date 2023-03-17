package net.trustgames.core.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.trustgames.core.managers.LuckPermsManager;
import net.trustgames.core.player.data.config.PlayerDataType;
import org.bukkit.entity.Player;

public final class MiniMessageUtils {

    /**
     * MiniMessage instance, which replaces
     * various tags in the message with values of the player
     * Some tags work only for offline players or online players!
     *
     * @param player Player to replace the tags with info of
     * @return new MiniMessage with formatter ready
     */
    public static MiniMessage format(Player player) {
        String playerName = player.getName();
        Component prefix = LuckPermsManager.getPlayerPrefix(player);
        if (!prefix.equals(Component.text(""))) {
            prefix = prefix.append(Component.text(" "));
        }

        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(StandardTags.defaults())
                        .resolver(TagResolver.resolver("player_name", Tag.selfClosingInserting(Component.text(
                                playerName))))
                        .resolver(TagResolver.resolver("prefix", Tag.selfClosingInserting(prefix)))
                        .build()
                )
                .build();
    }

    /**
     * MiniMessage instance, which replaces
     * {@literal the <component> tag in the message with the given Component}
     *
     * @param component Component to replace the tag with
     * @return new MiniMessage with formatter ready
     */
    public static MiniMessage addComponent(Component component) {
        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(StandardTags.defaults())
                        .resolver(TagResolver.resolver("component", Tag.selfClosingInserting(component)))
                        .build()
                )
                .build();
    }

    /**
     * MiniMessage instance, which replaces
     * various currency tags in the message with
     * data values of the player
     *
     * @param playerName Name of the Player to replace the tags with info of
     * @return new MiniMessage with formatter ready
     */
    public static MiniMessage playerData(String playerName, PlayerDataType dataType, String value) {
        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(StandardTags.defaults())
                        .resolver(TagResolver.resolver("player_name", Tag.selfClosingInserting(Component.text(
                                playerName))))
                        .resolver(TagResolver.resolver("player_data", Tag.selfClosingInserting(
                                Component.text(dataType.name().toLowerCase()))))
                        .resolver(TagResolver.resolver("value", Tag.selfClosingInserting(Component.text(value))))
                        .build()
                )
                .build();
    }
}
