package net.trustgames.core.managers;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.trustgames.core.Core;
import net.trustgames.core.chat.ChatDecoration;
import net.trustgames.core.chat.MessageLimiter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles limiting of the chat and adding a prefix
 * to the players name.
 */
public class ChatManager implements Listener {

    private final MessageLimiter messageLimiter;
    private final ChatDecoration chatDecoration;


    public ChatManager(Core core) {
        messageLimiter = new MessageLimiter(core);
        chatDecoration = new ChatDecoration(core);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        messageLimiter.limit(event);
        if (!event.isCancelled()) {
            chatDecoration.decorate(event);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        messageLimiter.onPlayerQuit(event);
    }
}