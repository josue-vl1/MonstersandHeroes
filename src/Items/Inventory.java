package Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds all items owned by a hero (or potentially a party).
 *
 * Responsibilities:
 *  - store a list of {@link Item} objects
 *  - provide type-specific views (Weapons, Armor, Spells, Potions)
 *  - enforce encapsulation by returning unmodifiable lists externally
 */
public class Inventory {

    /**
     * Backing list of all items in this inventory (any subclass of Item).
     */
    private final List<Item> items = new ArrayList<>();

    /**
     * Add a new item to the inventory.
     *
     * @param item item to add; ignored if null
     */
    public void addItem(Item item) {
        if (item != null) {
            items.add(item);
        }
    }

    /**
     * Remove a single occurrence of the given item from the inventory.
     *
     * @param item item to remove
     * @return true if the inventory contained the item and it was removed,
     *         false otherwise
     */
    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    /**
     * Get a read-only view of all items.
     * Callers cannot modify the underlying list directly; they must use
     * {@link #addItem(Item)} or {@link #removeItem(Item)}.
     * @return unmodifiable list of all items
     */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * @return a new list containing only the Weapons in this inventory.
     */
    public List<Weapon> getWeapons() {
        List<Weapon> result = new ArrayList<>();
        for (Item i : items) {
            if (i instanceof Weapon) {
                result.add((Weapon) i);
            }
        }
        return result;
    }

    /**
     * @return a new list containing only the Armor items in this inventory.
     */
    public List<Armor> getArmors() {
        List<Armor> result = new ArrayList<>();
        for (Item i : items) {
            if (i instanceof Armor) {
                result.add((Armor) i);
            }
        }
        return result;
    }

    /**
     * @return a new list containing only the Spells in this inventory.
     */
    public List<Spell> getSpells() {
        List<Spell> result = new ArrayList<>();
        for (Item i : items) {
            if (i instanceof Spell) {
                result.add((Spell) i);
            }
        }
        return result;
    }

    /**
     * @return a new list containing only the Potions in this inventory.
     */
    public List<Potion> getPotions() {
        List<Potion> result = new ArrayList<>();
        for (Item i : items) {
            if (i instanceof Potion) {
                result.add((Potion) i);
            }
        }
        return result;
    }

    /**
     * Debug-friendly summary of the inventory contents.
     */
    @Override
    public String toString() {
        return "Inventory{" + items + '}';
    }
}
