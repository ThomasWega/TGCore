package net.trustgames.core.managers.gui;

import net.trustgames.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class GUIListener implements Listener {

    private final GUIManager guiManager;

    public GUIListener(Core core) {
        this.guiManager = core.getGuiManager();
        Bukkit.getServer().getPluginManager().registerEvents(this, core);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        guiManager.handleClick(event);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        guiManager.handleOpen(event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        guiManager.handleClose(event);
    }
}
