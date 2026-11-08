package fr.toni_4819.pvpPveLevelingSystem.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Holder of the levels menu: identifies the inventory and keeps its page and button slots.
 */
public class LevelsMenu implements InventoryHolder {

    public enum Action { PREVIOUS_PAGE, NEXT_PAGE, CLOSE }

    private final int page;
    private final Map<Integer, Action> actions = new HashMap<>();
    private Inventory inventory;

    public LevelsMenu(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public Action getAction(int slot) {
        return actions.get(slot);
    }

    void setAction(int slot, Action action) {
        actions.put(slot, action);
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NonNull Inventory getInventory() {
        return inventory;
    }
}
