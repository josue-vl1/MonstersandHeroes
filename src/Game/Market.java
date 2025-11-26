package Game;

import Entities.Hero;
import Items.*;
import java.util.*;

/**
 * Represents a single market on the map.
 *
 * Responsibilities:
 *  - manage this market's stock of items
 *  - handle buy/sell menus for heroes
 *  - enforce "unique items" by removing bought items from stock
 *
 * It does NOT know about the board or tile positions.
 */
public class Market {

    /**
     * Scanner used for all market input.
     */
    private final Scanner scanner;

    /**
     * For each item in this market, how many copies are still available.
     */
    private final Map<Item, Integer> quantities;

    /**
     * Items currently for sale in this market (may shrink as stock runs out).
     */
    private final List<Item> stock;

    /**
     * Create a new Market with an initial stock and quantities based on party size.
     * Rough rules:
     *     Weapons & Armor: one copy per party member.
     *     Potions & Spells: ten copies per party member.
     *     Other items: one copy per party member (fallback).
     *
     * @param initialStock list of distinct items to sell in this market
     * @param partySize    number of heroes in the party (used to scale quantities)
     * @param scanner      input source for market menus
     */
    public Market(List<Item> initialStock, int partySize, Scanner scanner) {
        this.stock = new ArrayList<>(initialStock);
        this.quantities = new HashMap<>();
        this.scanner = scanner;

        for (Item item : stock) {
            int qty;
            if (item instanceof Items.Weapon || item instanceof Items.Armor) {
                qty = partySize;
            } else if (item instanceof Items.Potion || item instanceof Items.Spell) {
                qty = partySize * 10;
            } else {
                qty = partySize;
            }
            quantities.put(item, qty);
        }
    }

    /**
     * Entry point: open the market for the given party.
     * Lets the player pick which hero is trading, and then
     * opens that hero's buy/sell menu.
     *
     * @param party current party (used to choose which hero buys/sells)
     */
    public void open(Party party) {
        if (party == null || party.getMembers().isEmpty()) {
            System.out.println("No heroes in the party to trade.");
            return;
        }

        boolean inMarket = true;
        while (inMarket) {
            System.out.println("\n=== Market ===");
            System.out.println("Choose a hero to trade for, or 0 to leave the market.\n");

            List<Hero> heroes = party.getMembers();
            for (int i = 0; i < heroes.size(); i++) {
                Hero h = heroes.get(i);
                System.out.printf(
                        "%d) %s (Lvl %d, Gold %d)%n",
                        i + 1,
                        h.getName(),
                        h.getLevel(),
                        h.getGold()
                );
            }

            int choice = readIntInRange("Your choice: ", 0, heroes.size());
            if (choice == 0) {
                inMarket = false;
            } else {
                Hero selectedHero = heroes.get(choice - 1);
                handleHeroMarketMenu(selectedHero);
            }
        }
    }

    // ================= HERO MENU =================

    /**
     * Per–hero market menu.
     * The user can:
     *     Buy items for this hero.
     *     Sell items from this hero's inventory.
     *
     * @param hero hero currently interacting with the market
     */
    private void handleHeroMarketMenu(Hero hero) {
        boolean done = false;
        while (!done) {
            System.out.println("\n=== Market: " + hero.getName() + " ===");
            System.out.println("Level: " + hero.getLevel() +
                    " | Gold: " + hero.getGold());
            System.out.println("1) Buy items");
            System.out.println("2) Sell items");
            System.out.println("0) Back to hero selection");

            int choice = readIntInRange("Choose an option: ", 0, 2);
            switch (choice) {
                case 1:
                    handleBuy(hero);
                    break;
                case 2:
                    handleSell(hero);
                    break;
                case 0:
                default:
                    done = true;
                    break;
            }
        }
    }

    // ================= BUYING =================

    /**
     * Top-level "buy" menu for a hero:
     * choose which category to view (weapons, armor, potions, spells).
     */
    private void handleBuy(Hero hero) {
        boolean done = false;
        while (!done) {
            System.out.println("\n=== Buy Menu for " + hero.getName() + " ===");
            System.out.println("Gold: " + hero.getGold());
            System.out.println("1) Weapons");
            System.out.println("2) Armor");
            System.out.println("3) Potions");
            System.out.println("4) Spells");
            System.out.println("0) Back");

            int choice = readIntInRange("Choose a category: ", 0, 4);
            switch (choice) {
                case 1:
                    buyFromList(hero, getWeaponsInStock(), "Weapons");
                    break;
                case 2:
                    buyFromList(hero, getArmorsInStock(), "Armor");
                    break;
                case 3:
                    buyFromList(hero, getPotionsInStock(), "Potions");
                    break;
                case 4:
                    buyFromList(hero, getSpellsInStock(), "Spells");
                    break;
                case 0:
                default:
                    done = true;
                    break;
            }
        }
    }

    /**
     * Buying loop for a specific item category.
     * Shows a table of items + quantities, lets the user pick one,
     * and calls {@link #attemptPurchase(Hero, Item)}.
     * Sold-out items vanish when the list is reloaded.
     *
     * @param hero         hero making the purchase
     * @param items        current list of items in this category that are in stock
     * @param categoryName human-readable category name (e.g., "Weapons")
     */
    private void buyFromList(Hero hero, List<? extends Item> items, String categoryName) {
        if (items.isEmpty()) {
            System.out.println("No " + categoryName.toLowerCase() + " available for purchase in this market.");
            return;
        }

        boolean done = false;
        while (!done) {
            System.out.println("\n=== " + categoryName + " for sale ===");
            System.out.println("Gold: " + hero.getGold());

            printItemTableWithQuantity(items, true);

            System.out.println("Enter the number of the item to buy, or 0 to go back.");
            int choice = readIntInRange("Your choice: ", 0, items.size());
            if (choice == 0) {
                done = true;
            } else {
                Item item = items.get(choice - 1);
                if (attemptPurchase(hero, item)) {
                    if (item instanceof Weapon) {
                        items = getWeaponsInStock();
                    } else if (item instanceof Armor) {
                        items = getArmorsInStock();
                    } else if (item instanceof Potion) {
                        items = getPotionsInStock();
                    } else if (item instanceof Spell) {
                        items = getSpellsInStock();
                    }

                    if (items.isEmpty()) {
                        System.out.println("No more " + categoryName.toLowerCase() + " in this market.");
                        done = true;
                    }
                }
            }
        }
    }

    /**
     * Performs the actual purchase, enforcing:
     *     Level requirement (hero level >= item level).
     *     Quantity requirement (item not sold out).
     *     Gold requirement (hero has enough gold).
     * On success:
     *     Gold is deducted from the hero.
     *     Item is added to hero's inventory.
     *     Market quantity for that item is decreased by 1.
     *
     * @return true if purchase succeeded, false otherwise
     */
    private boolean attemptPurchase(Hero hero, Item item) {
        int price = item.getPrice();
        if (hero.getLevel() < item.getLevel()) {
            System.out.printf(
                    "Cannot buy %s: requires level %d, but %s is level %d.%n",
                    item.getName(), item.getLevel(), hero.getName(), hero.getLevel()
            );
            return false;
        }

        int currentQty = getQuantity(item);
        if (currentQty <= 0) {
            System.out.println("This item is sold out.");
            return false;
        }

        if (!hero.spendGold(price)) {
            System.out.printf(
                    "Cannot buy %s: costs %d gold, but %s has only %d gold.%n",
                    item.getName(), price, hero.getName(), hero.getGold()
            );
            return false;
        }

        hero.getInventory().addItem(item);
        quantities.put(item, currentQty - 1);

        System.out.printf(
                "%s bought %s for %d gold.%n",
                hero.getName(), item.getName(), price
        );
        return true;
    }

    // ================= SELLING =================

    /**
     * Sell menu for a hero:
     * shows their inventory and allows them to sell items for half price.
     * When selling:
     *     Item is removed from the hero's inventory.
     *     Hero gains half the original price in gold.
     *     Market stock/quantity of that item increases by 1.
     */
    private void handleSell(Hero hero) {
        Inventory inventory = hero.getInventory();
        List<Item> items = inventory.getItems();

        if (items.isEmpty()) {
            System.out.println(hero.getName() + " has no items to sell.");
            return;
        }

        boolean done = false;
        while (!done) {
            System.out.println("\n=== Sell Menu for " + hero.getName() + " ===");
            System.out.println("Gold: " + hero.getGold());
            System.out.println("Items in inventory:");

            printItemTable(items, true);

            System.out.println("Enter the number of the item to sell, or 0 to go back.");
            int choice = readIntInRange("Your choice: ", 0, items.size());
            if (choice == 0) {
                done = true;
            } else {
                Item item = items.get(choice - 1);
                int sellPrice = item.getPrice() / 2;

                if (inventory.removeItem(item)) {
                    hero.addGold(sellPrice);
                    if (!stock.contains(item)) {
                        stock.add(item);
                    }
                    int currentQty = getQuantity(item);
                    quantities.put(item, currentQty + 1);

                    System.out.printf("%s sold %s for %d gold.%n",
                            hero.getName(), item.getName(), sellPrice);
                    items = inventory.getItems();
                    if (items.isEmpty()) {
                        System.out.println("No more items to sell.");
                        done = true;
                    }
                } else {
                    System.out.println("Could not remove item from inventory.");
                }
            }
        }
    }

    // ================= STOCK QUERIES =================

    /**
     * @return list of all weapons in stock that still have quantity > 0
     */
    private List<Weapon> getWeaponsInStock() {
        List<Weapon> result = new ArrayList<>();
        for (Item item : stock) {
            if (item instanceof Weapon && getQuantity(item) > 0) {
                result.add((Weapon) item);
            }
        }
        return result;
    }

    /**
     * @return list of all armor items in stock that still have quantity > 0
     */
    private List<Armor> getArmorsInStock() {
        List<Armor> result = new ArrayList<>();
        for (Item item : stock) {
            if (item instanceof Armor && getQuantity(item) > 0) {
                result.add((Armor) item);
            }
        }
        return result;
    }

    /**
     * @return list of all potions in stock that still have quantity > 0
     */
    private List<Potion> getPotionsInStock() {
        List<Potion> result = new ArrayList<>();
        for (Item item : stock) {
            if (item instanceof Potion && getQuantity(item) > 0) {
                result.add((Potion) item);
            }
        }
        return result;
    }

    /**
     * @return list of all spells in stock that still have quantity > 0
     */
    private List<Spell> getSpellsInStock() {
        List<Spell> result = new ArrayList<>();
        for (Item item : stock) {
            if (item instanceof Spell && getQuantity(item) > 0) {
                result.add((Spell) item);
            }
        }
        return result;
    }

    /**
     * Get how many copies of a given item are left in this market.
     */
    private int getQuantity(Item item) {
        return quantities.getOrDefault(item, 0);
    }

    // ================= PRINT HELPERS =================

    /**
     * Prints a generic table for items (any type).
     * <p>
     * Used mainly for selling, where we don't need a quantity column.
     *
     * @param items        items to display
     * @param includeIndex whether to show an index column (#)
     */
    private void printItemTable(List<? extends Item> items, boolean includeIndex) {
        if (items == null || items.isEmpty()) {
            System.out.println("  (none)");
            return;
        }

        String indexFormat = includeIndex ? "%-3s " : "";
        Item first = items.get(0);
        String[] headers = first.getColumnHeaders();
        if (includeIndex) {
            System.out.printf(indexFormat, "#");
        }
        for (String header : headers) {
            System.out.printf("%-15s ", header);
        }
        System.out.println();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            String[] values = item.getColumnValues();

            if (includeIndex) {
                System.out.printf(indexFormat, String.valueOf(i + 1));
            }
            for (String value : values) {
                System.out.printf("%-15s ", value);
            }
            System.out.println();
        }
    }

    /**
     * Same as {@link #printItemTable(List, boolean)}, but adds a "Qty"
     * column at the end, showing how many copies remain in this market.
     * Used for buying, so players can see stock levels.
     */
    private void printItemTableWithQuantity(List<? extends Item> items, boolean includeIndex) {
        if (items == null || items.isEmpty()) {
            System.out.println("  (none)");
            return;
        }
        String indexFormat = includeIndex ? "%-3s " : "";
        Item first = items.get(0);
        String[] headers = first.getColumnHeaders();
        if (includeIndex) {
            System.out.printf(indexFormat, "#");
        }
        for (String header : headers) {
            System.out.printf("%-15s ", header);
        }
        System.out.printf("%-5s%n", "Qty");

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            String[] values = item.getColumnValues();
            int qty = getQuantity(item);

            if (includeIndex) {
                System.out.printf(indexFormat, String.valueOf(i + 1));
            }
            for (String value : values) {
                System.out.printf("%-15s ", value);
            }
            System.out.printf("%-5d%n", qty);
        }
    }

    // ================= INPUT HELPER =================

    /**
     * Utility to read an integer in the range [min, max] from the user,
     * re-prompting on invalid or out-of-range input.
     */
    private int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }
}
