package net.trustgames.core.player.activity.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.trustgames.core.Core;
import net.trustgames.core.cache.PlayerDataCache;
import net.trustgames.core.command.TrustCommand;
import net.trustgames.core.config.CommandConfig;
import net.trustgames.core.config.CorePermissionsConfig;
import net.trustgames.core.player.data.config.PlayerDataType;
import net.trustgames.core.managers.database.DatabaseManager;
import net.trustgames.core.player.activity.PlayerActivityFetcher;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Is used to get the logged player's activity by the activity id, rather
 * than the player's name. It always prints just one result in the chat,
 * where player can click on each data, and it will be copied to his clipboard.
 */
public final class ActivityIdCommand extends TrustCommand {

    private final Core core;
    private final DatabaseManager databaseManager;


    public ActivityIdCommand(Core core) {
        super(CorePermissionsConfig.STAFF.permission);
        this.core = core;
        this.databaseManager = core.getDatabaseManager();
    }

    @Override
    @AllowConsole
    public void execute(CommandSender sender, String[] args, String label) {
        if (databaseManager.isMySQLDisabled()) {
            sender.sendMessage(CommandConfig.COMMAND_DATABASE_OFF.getText());
            return;
        }
        if (args.length != 1) {
            sender.sendMessage(CommandConfig.COMMAND_INVALID_ARG.getText().append(
                    Component.text(" Use /activity-id <ID>", NamedTextColor.DARK_GRAY)));
            return;
        }

        String stringId = args[0];
        long id;
        try {
            id = Long.parseLong(stringId);
        } catch (NumberFormatException e) {
            sender.sendMessage(CommandConfig.COMMAND_INVALID_ID.addComponent(Component.text(stringId)));
            return;
        }

        // print the data to the chat
        printData(sender, id);
    }

    /**
     * Get the data from the result set and put it in a Component List.
     * Then loop through the list and for each one, send the message to the player.
     *
     * @param sender Command sender
     * @param id     Activity id
     */
    private void printData(CommandSender sender, long id) {
        PlayerActivityFetcher activityFetcher = new PlayerActivityFetcher(core);
        activityFetcher.fetchByID(id, activity -> {
            if (activity == null) {
                sender.sendMessage(CommandConfig.COMMAND_NO_ID_DATA.addComponent(Component.text(id)));
                return;
            }

            // get all the data from the resultSet
            long resultId = activity.getId();
            UUID uuid = activity.getUuid();
            String ip = activity.getIp();
            String action = activity.getAction();
            Timestamp time = activity.getTime();

            PlayerDataCache playerDataCache = new PlayerDataCache(core, uuid, PlayerDataType.NAME);
            playerDataCache.get(name -> {
                if (name == null){
                    name = "ERROR";
                }
                // list of component messages
                List<Component> chatMessage = List.of(
                        Component.text(ChatColor.DARK_GRAY + "------------------------"),
                        Component.text(ChatColor.WHITE + "Name: " +
                                ChatColor.RED + name).clickEvent(ClickEvent.copyToClipboard(name)),
                        Component.text(ChatColor.WHITE + "IP: " +
                                ChatColor.YELLOW + ip).clickEvent(ClickEvent.copyToClipboard(ip)),
                        Component.text(ChatColor.WHITE + "UUID: " +
                                ChatColor.GRAY + uuid).clickEvent(ClickEvent.copyToClipboard(uuid.toString())),
                        Component.text(""),
                        Component.text(ChatColor.WHITE + "Action: " +
                                ChatColor.GOLD + action).clickEvent(ClickEvent.copyToClipboard(action)),
                        Component.text(ChatColor.WHITE + "Date: " +
                                        ChatColor.GREEN + time.toLocalDateTime().toLocalDate())
                                .clickEvent(ClickEvent.copyToClipboard(time.toLocalDateTime().toLocalDate().toString())),
                        Component.text(ChatColor.WHITE + "Time: " +
                                        ChatColor.DARK_GREEN + time.toLocalDateTime().toLocalTime() + " " +
                                        ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.ROOT))
                                .clickEvent(ClickEvent.copyToClipboard(time.toLocalDateTime().toLocalTime() + " " +
                                        ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.ROOT))),
                        Component.text(""),
                        Component.text(ChatColor.WHITE + "ID: " +
                                ChatColor.DARK_PURPLE + resultId).clickEvent(ClickEvent.copyToClipboard(String.valueOf(resultId))),
                        Component.text(ChatColor.DARK_GRAY + "------------------------")
                );
                // loop through the list and for each, send a message
                for (Component s : chatMessage) {
                    sender.sendMessage(s);
                }
            });
        });
    }
}
