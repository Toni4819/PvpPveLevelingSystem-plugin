package fr.toni_4819.pvpPveLevelingSystem.gui;

import fr.toni_4819.pvpPveLevelingSystem.PvpPveLevelingSystem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GuiListener implements Listener {

    private final PvpPveLevelingSystem plugin;

    public GuiListener(PvpPveLevelingSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof LevelsMenu menu)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        LevelsMenu.Action action = menu.getAction(event.getRawSlot());
        if (action == null) return;

        switch (action) {
            case PREVIOUS_PAGE -> plugin.getGuiManager().open(player, menu.getPage() - 1);
            case NEXT_PAGE -> plugin.getGuiManager().open(player, menu.getPage() + 1);
            case CLOSE -> player.closeInventory();
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof LevelsMenu) {
            event.setCancelled(true);
        }
    }
}
