package Game;

import Entities.*;
import Items.*;
import Utility.GameData;

import java.util.*;

/**
 * Orchestrates the high–level flow of the Legends game:
 *     Shows title screen and instructions.
 *     Handles hero party selection.
 *     Initializes the board, markets, and party position.
 *     Runs the main game loop: movement, markets, and random battles.
 *
 * Domain objects like Board, Party, Hero, Market, and Battle
 * keep their own logic; Game just coordinates them.
 */
public class Game {

    /**
     * Size (width and height) of the square board.
     */
    private static final int boardSize = 8;

    /**
     * Wrapper for all pre-loaded data: heroes, monsters, items, etc.
     */
    private final GameData gameData;

    /**
     * Scanner for reading user input.
     */
    private final Scanner scanner;

    /**
     * PRNG used for random battles and monster selection.
     */
    private final Random random = new Random();

    /**
     * The current game board.
     */
    private Board board;

    /**
     * The hero party currently controlled by the player.
     */
    private Party party;

    /**
     * 2D array of Market references aligned with the Board grid.
     * For a market tile at (r, c), markets[r][c] holds the Market instance.
     */
    private Market[][] markets;

    /**
     * Construct a Game controller with the given game data and input.
     *
     * @param gameData pre-loaded heroes, monsters, and items
     * @param scanner  input source for the console UI
     */
    public Game(GameData gameData, Scanner scanner) {
        this.gameData = gameData;
        this.scanner = scanner;
    }

    /**
     * Public entry point for running the game.
     * Shows the title screen, then processes user choices:
     * play, view instructions, or quit.
     */
    public void start() {
        boolean running = true;

        while (running) {
            printTitleScreen();

            int choice = readIntInRange("Select an option (1-3): ", 1, 3);
            switch (choice) {
                case 1:
                    initializeBoardAndParty();
                    runMainLoop();
                    running = false;
                    break;
                case 2:
                    printInstructions();
                    break;
                case 3:
                    System.out.println("Goodbye!");
                    running = false;
                    break;
            }
        }
    }

    /**
     * Create a board (with user confirmation) and select the party.
     * Also initializes markets after the party is known.
     */
    private void initializeBoardAndParty() {
        this.board = createAndConfirmBoard(boardSize);
        List<Hero> selectedHeroes = selectParty();
        this.party = new Party(selectedHeroes, board.getStartRow(), board.getStartCol());
        int partySize = selectedHeroes.size();
        initializeMarkets(partySize);

        System.out.println("\nParty created with " + selectedHeroes.size() + " hero(es).");
        System.out.println("Starting game...");
        System.out.println("Use W/A/S/D to move, I for inventory, Q to quit.\n");
    }

    /**
     * Initialize a Market for each market tile on the board.
     * Items are taken from GameData and distributed across markets
     * in a round–robin fashion.
     *
     * @param partySize how many heroes are in the party (used by Market)
     */
    private void initializeMarkets(int partySize) {
        List<int[]> positions = board.getMarketPositions();
        if (positions.isEmpty()) {
            return;
        }

        int size = board.getSize();
        markets = new Market[size][size];
        List<Item> pool = new ArrayList<Item>();
        pool.addAll(gameData.getWeapons());
        pool.addAll(gameData.getArmors());
        pool.addAll(gameData.getPotions());
        pool.addAll(gameData.getSpells());

        Collections.shuffle(pool, new Random());

        int marketCount = positions.size();
        List<List<Item>> perMarketStock = new ArrayList<List<Item>>();
        for (int i = 0; i < marketCount; i++) {
            perMarketStock.add(new ArrayList<Item>());
        }

        for (int i = 0; i < pool.size(); i++) {
            perMarketStock.get(i % marketCount).add(pool.get(i));
        }

        for (int i = 0; i < marketCount; i++) {
            int[] pos = positions.get(i);
            int row = pos[0];
            int col = pos[1];
            Market market = new Market(perMarketStock.get(i), partySize, scanner);
            markets[row][col] = market;
        }
    }

    /**
     * Ask the user to accept/re-roll randomly generated boards until they like one.
     *
     * @param size board size
     * @return the chosen Board
     */
    private Board createAndConfirmBoard(int size) {
        while (true) {
            Board candidate = new Board(size);
            Party previewParty = new Party(candidate.getStartRow(), candidate.getStartCol());

            System.out.println("\nPreview of generated map:");
            candidate.print(previewParty);
            System.out.print("Do you want to use this map? (Y to accept, anything else to reroll): ");

            String line = scanner.nextLine().trim();
            if (!line.isEmpty() && Character.toUpperCase(line.charAt(0)) == 'Y') {
                return candidate;
            }

            System.out.println("Okay, generating a new map...\n");
        }
    }

    /**
     * Main game loop: show map, read command, and dispatch high-level actions.
     *     Movement with W/A/S/D.
     *     Open party inventory with I.
     *     Quit with Q.
     */
    private void runMainLoop() {
        boolean running = true;
        while (running) {
            board.print(party);
            System.out.print("Move (W/A/S/D), I = Inventory, or Q to quit: ");

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            char cmd = Character.toUpperCase(line.charAt(0));
            switch (cmd) {
                case 'Q':
                    running = false;
                    break;
                case 'W':
                case 'A':
                case 'S':
                case 'D':
                    party.move(cmd, board);
                    handleTileAfterMove();
                    break;
                case 'I':
                    openInventoryMenu();
                    break;
                default:
                    System.out.println("Unknown command. Use W/A/S/D to move, I for inventory, Q to quit.");
                    break;
            }
        }

        System.out.println("Goodbye!");
    }

    /**
     * Let the player choose 1–3 distinct heroes from the available pool in GameData.
     * Uses a two-level menu:
     *     Choose class (Warrior / Paladin / Sorcerer).
     *     Choose a specific hero from that class.
     *
     * @return list of selected Hero objects
     */
    private List<Hero> selectParty() {
        List<Hero> allHeroes = gameData.getHeroes();
        if (allHeroes == null || allHeroes.isEmpty()) {
            throw new IllegalStateException("No heroes available in game data.");
        }

        while (true) {
            System.out.println("\n=== Hero Selection ===");
            System.out.println("You may choose between 1 and " + Party.maxMembers + " heroes.");

            int partySize = readIntInRange(
                    "How many heroes do you want in your party? (1-" + Party.maxMembers + "): ",
                    1,
                    Party.maxMembers
            );

            List<Hero> selected = new ArrayList<Hero>();
            for (int i = 0; i < partySize; i++) {
                Hero hero = chooseHeroViaClassMenu(allHeroes, selected, i + 1);
                selected.add(hero);
            }

            System.out.println("\nYou selected:");
            printPartyPreview(selected);

            System.out.print("Confirm this party? (Y to confirm, anything else to reselect): ");
            String confirm = scanner.nextLine().trim();

            if (!confirm.isEmpty() && Character.toUpperCase(confirm.charAt(0)) == 'Y') {
                return selected;
            }

            System.out.println("Okay, let’s choose again.");
        }
    }

    /**
     * First-level menu: choose hero class (Paladins, Warriors, Sorcerers).
     *
     * @param allHeroes       all heroes from GameData
     * @param alreadySelected heroes already in the party
     * @param heroNumber      index of hero being selected (1-based)
     * @return the chosen Hero
     */
    private Hero chooseHeroViaClassMenu(List<Hero> allHeroes, List<Hero> alreadySelected, int heroNumber) {
        while (true) {
            Map<String, List<Hero>> heroesByType = groupHeroesByType(allHeroes);
            Map<Integer, String> indexToType = new LinkedHashMap<Integer, String>();
            int menuIndex = 1;

            System.out.println("\nAvailable hero classes:");
            for (Map.Entry<String, List<Hero>> entry : heroesByType.entrySet()) {
                String typeName = entry.getKey();
                List<Hero> heroesOfType = entry.getValue();

                long availableCount = 0;
                for (Hero h : heroesOfType) {
                    if (!alreadySelected.contains(h)) {
                        availableCount++;
                    }
                }

                if (availableCount > 0) {
                    String label = typeName + "s";
                    System.out.printf("%d) %s (%d available)%n", menuIndex, label, availableCount);
                    indexToType.put(menuIndex, typeName);
                    menuIndex++;
                }
            }

            if (indexToType.isEmpty()) {
                throw new IllegalStateException("No heroes left to choose.");
            }

            int choice = readIntInRange(
                    "Select a hero class for hero #" + heroNumber + ": ",
                    1,
                    indexToType.size()
            );

            String chosenType = indexToType.get(choice);
            Hero hero = chooseHeroFromClass(chosenType, heroesByType.get(chosenType), alreadySelected, heroNumber);

            if (hero != null) {
                return hero;
            }

        }
    }

    /**
     * Second-level menu: show a table of heroes of a given class,
     * and let the user select one or go back.
     */
    private Hero chooseHeroFromClass(String typeName,
                                     List<Hero> heroesOfType,
                                     List<Hero> alreadySelected,
                                     int heroNumber) {

        while (true) {
            List<Hero> available = new ArrayList<Hero>();
            for (Hero h : heroesOfType) {
                if (!alreadySelected.contains(h)) {
                    available.add(h);
                }
            }

            if (available.isEmpty()) {
                System.out.println("No more available " + typeName + "s.");
                return null;
            }

            System.out.println("\n=== " + typeName + "s ===");
            System.out.println("Choose hero #" + heroNumber + " from the list below.\n");


            System.out.printf(
                    "%-3s %-20s %-5s %-11s %-11s %-6s %-6s %-6s %-7s %-7s%n",
                    "#", "Name", "Lvl", "HP", "MP", "Str", "Dex", "Agi", "Gold", "Exp"
            );


            for (int i = 0; i < available.size(); i++) {
                Hero h = available.get(i);
                String hpStr = h.getHP() + "/" + h.getBaseHP();
                String mpStr = h.getMP() + "/" + h.getBaseMP();

                System.out.printf(
                        "%-3d %-20s %-5d %-11s %-11s %-6d %-6d %-6d %-7d %-7d%n",
                        (i + 1),
                        h.getName(),
                        h.getLevel(),
                        hpStr,
                        mpStr,
                        h.getStrength(),
                        h.getDexterity(),
                        h.getAgility(),
                        h.getGold(),
                        h.getExp()
                );
            }

            System.out.println();
            System.out.println("Enter the number of the hero to select, or 0 to go back to hero classes.");

            int choice = readIntInRange(
                    "Your choice for hero #" + heroNumber + ": ",
                    0,
                    available.size()
            );

            if (choice == 0) {
                return null;
            }

            return available.get(choice - 1);
        }
    }

    /**
     * Handle the tile the party has just moved onto:
     *     If it's a market tile, optionally open the market.
     *     If it's a common tile, possibly start a random battle.
     */
    private void handleTileAfterMove() {
        Tile current = board.getTile(party.getRow(), party.getCol());

        if (current.isMarket()) {

            if (markets == null) {
                System.out.println("Error: markets not initialized.");
                return;
            }

            int row = party.getRow();
            int col = party.getCol();
            Market market = markets[row][col];

            if (market == null) {
                System.out.println("Error: no market assigned to this tile.");
                return;
            }

            while (true) {
                System.out.print("Do you want to enter the market? (Y/N): ");
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                char ch = Character.toUpperCase(line.charAt(0));
                if (ch == 'Y') {
                    market.open(party);
                    break;
                } else if (ch == 'N') {
                    System.out.println("You decide not to enter the market.");
                    break;
                } else {
                    System.out.println("Please enter Y or N.");
                }
            }
        } else {
            maybeStartRandomBattle();
        }
    }

    /**
     * Group heroes by their class name (Paladin, Warrior, Sorcerer, etc.).
     */
    private Map<String, List<Hero>> groupHeroesByType(List<Hero> allHeroes) {
        Map<String, List<Hero>> result = new LinkedHashMap<String, List<Hero>>();
        for (Hero hero : allHeroes) {
            String typeName = hero.getClass().getSimpleName();
            if (!result.containsKey(typeName)) {
                result.put(typeName, new ArrayList<Hero>());
            }
            result.get(typeName).add(hero);
        }
        return result;
    }

    /**
     * Randomly decide if a battle starts on a common tile.
     * If so, create a balanced monster group and run a Battle.
     */
    private void maybeStartRandomBattle() {
        double chance = 0.3;

        if (random.nextDouble() > chance) {
            return;
        }

        System.out.println("You feel a dark presence...");

        List<Monster> monsters = createMonstersForEncounter();
        if (monsters.isEmpty()) {
            return;
        }

        Battle battle = new Battle(party, monsters, scanner);
        boolean heroesWon = battle.run();

        if (!heroesWon) {
            System.exit(0);
        }
    }

    /**
     * Compute a reference party level (e.g., average level of all heroes),
     * used to choose appropriate monsters.
     */
    private int getPartyReferenceLevel() {
        int sum = 0;
        int count = 0;

        for (Hero h : party.getMembers()) {
            sum += h.getLevel();
            count++;
        }

        if (count == 0) {
            return 1;
        }

        return Math.max(1, Math.round((float) sum / count));
    }

    /**
     * Create a list of monsters for an encounter, with levels roughly
     * around the party's reference level.
     * The number of monsters equals the party size, and monsters are
     * cloned from templates in GameData.
     */
    private List<Monster> createMonstersForEncounter() {
        List<Monster> allMonsters = gameData.getMonsters();
        if (allMonsters == null || allMonsters.isEmpty()) {
            System.out.println("No monsters available in game data.");
            return new ArrayList<Monster>();
        }

        int partySize = party.getMembers().size();
        int partyLevel = getPartyReferenceLevel();
        int levelTolerance = 1;
        int minLevel = Math.max(1, partyLevel - levelTolerance);
        int maxLevel = partyLevel + levelTolerance;

        List<Monster> candidates = new ArrayList<Monster>();
        for (Monster m : allMonsters) {
            int ml = m.getLevel();
            if (ml >= minLevel && ml <= maxLevel) {
                candidates.add(m);
            }
        }

        if (candidates.isEmpty()) {
            candidates = allMonsters;
        }

        List<Monster> result = new ArrayList<Monster>();

        for (int i = 0; i < partySize; i++) {
            Monster template = candidates.get(random.nextInt(candidates.size()));
            Monster copy = cloneMonster(template);
            result.add(copy);
        }

        return result;
    }

    /**
     * Clone a Monster template using the appropriate Builder
     * (Dragon, Exoskeleton, Spirit).
     */
    private Monster cloneMonster(Monster template) {
        String name   = template.getName();
        int level     = template.getLevel();
        int baseHP    = template.getBaseHP();
        int baseDmg   = template.getBaseDamage();
        int defense   = template.getDefense();
        int dodge     = template.getDodge();

        if (template instanceof Dragon) {
            return new Dragon.Builder(name, level)
                    .baseHP(baseHP)
                    .baseDamage(baseDmg)
                    .defense(defense)
                    .dodge(dodge)
                    .build();
        } else if (template instanceof Exoskeleton) {
            return new Exoskeleton.Builder(name, level)
                    .baseHP(baseHP)
                    .baseDamage(baseDmg)
                    .defense(defense)
                    .dodge(dodge)
                    .build();
        } else if (template instanceof Spirit) {
            return new Spirit.Builder(name, level)
                    .baseHP(baseHP)
                    .baseDamage(baseDmg)
                    .defense(defense)
                    .dodge(dodge)
                    .build();
        } else {
            throw new IllegalArgumentException(
                    "Unsupported monster type: " + template.getClass().getSimpleName());
        }
    }

    /**
     * Read an integer from the user, ensuring it is between min and max (inclusive).
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

    /**
     * Party-level inventory menu (from overworld).
     * Lets the player choose which hero's inventory to open.
     */
    private void openInventoryMenu() {
        List<Hero> heroes = party.getMembers();
        if (heroes.isEmpty()) {
            System.out.println("Your party is empty.");
            return;
        }

        while (true) {
            System.out.println("\n=== Party Inventory ===");
            for (int i = 0; i < heroes.size(); i++) {
                Hero h = heroes.get(i);
                System.out.printf(
                        "%d) %s (Lvl %d, HP %d/%d, MP %d/%d)%n",
                        i + 1,
                        h.getName(),
                        h.getLevel(),
                        h.getHP(), h.getBaseHP(),
                        h.getMP(), h.getBaseMP()
                );
            }
            System.out.println("0) Back to map");

            int choice = readIntInRange("Whose inventory do you want to open? ", 0, heroes.size());
            if (choice == 0) {
                return;
            }

            Hero selected = heroes.get(choice - 1);
            openHeroInventoryMenu(selected);
        }
    }

    /**
     * Single–hero inventory menu: shows what is equipped and
     * lets you equip items or inspect inventory.
     */
    private void openHeroInventoryMenu(Hero hero) {
        boolean stay = true;
        while (stay) {
            System.out.println("\n=== " + hero.getName() + " Inventory ===");
            System.out.println("Equipped weapon(s): " + describeEquippedWeapons(hero));
            System.out.println("Equipped armor : " +
                    (hero.getEquippedArmor() == null ? "none" : hero.getEquippedArmor().getName()));

            System.out.println("\n1) Change equipment");
            System.out.println("2) Show all items");
            System.out.println("0) Back");

            int choice = readIntInRange("Your choice: ", 0, 2);
            switch (choice) {
                case 1:
                    changeEquipment(hero);
                    break;
                case 2:
                    printHeroItems(hero);
                    break;
                case 0:
                    stay = false;
                    break;
            }
        }
    }

    /**
     * Simple listing of all items the hero carries, grouped by type,
     * and showing quantity and whether a row is currently equipped.
     */
    private void printHeroItems(Hero hero) {
        List<Item> items = hero.getInventory().getItems();

        System.out.println("\nItems for " + hero.getName() + ":");

        if (items == null || items.isEmpty()) {
            System.out.println("  (no items)");
            return;
        }

        Map<Class<?>, List<Item>> byType = new LinkedHashMap<Class<?>, List<Item>>();
        for (Item it : items) {
            Class<?> key = it.getClass();
            if (!byType.containsKey(key)) {
                byType.put(key, new ArrayList<Item>());
            }
            byType.get(key).add(it);
        }

        String equippedWeaponKey = null;
        String equippedArmorKey  = null;

        if (hero.getEquippedWeapon() != null) {
            equippedWeaponKey = joinColumns(hero.getEquippedWeapon().getColumnValues());
        }
        if (hero.getEquippedArmor() != null) {
            equippedArmorKey = joinColumns(hero.getEquippedArmor().getColumnValues());
        }

        for (Map.Entry<Class<?>, List<Item>> entry : byType.entrySet()) {
            List<Item> group = entry.getValue();
            if (group.isEmpty()) continue;

            Item first = group.get(0);
            String[] headers = first.getColumnHeaders();

            System.out.println("\n-- " + first.getClass().getSimpleName() + "s --");
            Map<String, Integer> qtyMap = new LinkedHashMap<String, Integer>();
            Map<String, Item> repMap = new LinkedHashMap<String, Item>();

            for (Item it : group) {
                String key = joinColumns(it.getColumnValues());
                Integer old = qtyMap.get(key);
                if (old == null) old = 0;
                qtyMap.put(key, old + 1);
                if (!repMap.containsKey(key)) {
                    repMap.put(key, it);
                }
            }

            System.out.printf("%-3s ", "#");
            for (String header : headers) {
                System.out.printf("%-15s ", header);
            }
            System.out.printf("%-10s %-5s%n", "Equipped", "Qty");

            int rowIndex = 1;
            for (Map.Entry<String, Integer> row : qtyMap.entrySet()) {
                String key = row.getKey();
                Item rep   = repMap.get(key);
                String[] values = rep.getColumnValues();
                int qty    = row.getValue();
                String equippedFlag = "";
                if (rep instanceof Weapon && key.equals(equippedWeaponKey)) {
                    equippedFlag = "Yes";
                } else if (rep instanceof Armor && key.equals(equippedArmorKey)) {
                    equippedFlag = "Yes";
                }

                System.out.printf("%-3d ", rowIndex++);
                for (String value : values) {
                    System.out.printf("%-15s ", value);
                }
                System.out.printf("%-10s %-5d%n", equippedFlag, qty);
            }
        }
    }

    /**
     * Helper to join column values into a unique string key.
     * (Used for grouping identical items.)
     */
    private String joinColumns(String[] cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append("|");
            sb.append(cols[i]);
        }
        return sb.toString();
    }

    /**
     * Menu for changing equipment (weapon/armor) of a given hero.
     *
     * @return true if something changed, false otherwise
     */
    private boolean changeEquipment(Hero hero) {
        boolean changed = false;

        boolean menuOpen = true;
        while (menuOpen) {
            System.out.println("\n=== Change equipment for " + hero.getName() + " ===");
            System.out.println("Current weapon(s): " + describeEquippedWeapons(hero));
            System.out.println("Current armor : " +
                    (hero.getEquippedArmor() == null ? "none" : hero.getEquippedArmor().getName()));
            System.out.println("1) Change weapon");
            System.out.println("2) Change armor");
            System.out.println("0) Back");

            int choice = readIntInRange("Your choice: ", 0, 2);
            switch (choice) {
                case 1:
                    if (changeWeapon(hero)) {
                        changed = true;
                    }
                    break;
                case 2:
                    if (changeArmor(hero)) {
                        changed = true;
                    }
                    break;
                case 0:
                    menuOpen = false;
                    break;
            }
        }

        return changed;
    }

    /**
     * Weapon-changing logic from the overworld/inventory context.
     */
    private boolean changeWeapon(Hero hero) {
        List<Item> all = hero.getInventory().getItems();
        List<Weapon> weapons = new ArrayList<Weapon>();
        for (Item it : all) {
            if (it instanceof Weapon) {
                weapons.add((Weapon) it);
            }
        }

        if (weapons.isEmpty()) {
            System.out.println("No weapons in inventory.");
            return false;
        }

        System.out.println("\nWeapons in inventory:");
        System.out.printf("%-3s %-20s %-5s %-10s%n",
                "#", "Name", "Lvl", "Damage");
        for (int i = 0; i < weapons.size(); i++) {
            Weapon w = weapons.get(i);
            System.out.printf("%-3d %-20s %-5d %-10d%n",
                    i + 1,
                    w.getName(),
                    w.getLevel(),
                    w.getDamageValue());
        }
        System.out.println("Enter the number of the weapon to equip, or 0 to cancel.");
        int choice = readIntInRange("Your choice: ", 0, weapons.size());
        if (choice == 0) {
            return false;
        }

        Weapon selected = weapons.get(choice - 1);
        boolean ok = hero.equipWeapon(selected);
        if (!ok) {
            System.out.printf("Could not equip %s.%n", selected.getName());
            return false;
        }

        System.out.printf(
                "%s now wields: %s%n",
                hero.getName(),
                describeEquippedWeapons(hero)
        );
        return true;
    }

    /**
     * Armor-changing logic from the overworld/inventory context.
     */
    private boolean changeArmor(Hero hero) {
        List<Item> all = hero.getInventory().getItems();
        List<Armor> armors = new ArrayList<Armor>();
        for (Item it : all) {
            if (it instanceof Armor) {
                armors.add((Armor) it);
            }
        }

        if (armors.isEmpty()) {
            System.out.println("No armor in inventory.");
            return false;
        }

        System.out.println("\nArmor in inventory:");
        System.out.printf("%-3s %-20s %-5s %-15s%n",
                "#", "Name", "Lvl", "DamageRed");
        for (int i = 0; i < armors.size(); i++) {
            Armor a = armors.get(i);
            System.out.printf("%-3d %-20s %-5d %-15d%n",
                    i + 1,
                    a.getName(),
                    a.getLevel(),
                    a.getDamageReduction());
        }
        System.out.println("Enter the number of the armor to equip, or 0 to cancel.");
        int choice = readIntInRange("Your choice: ", 0, armors.size());
        if (choice == 0) {
            return false;
        }

        Armor selected = armors.get(choice - 1);
        hero.setEquippedArmor(selected);
        System.out.printf("%s now wears %s.%n", hero.getName(), selected.getName());
        return true;
    }

    /**
     * Human-readable description of the hero's currently equipped weapon(s).
     * Shows both main-hand and off-hand, including hands required.
     */
    private String describeEquippedWeapons(Hero hero) {
        Weapon main = hero.getEquippedWeapon();
        Weapon off  = hero.getOffHandWeapon();

        if (main == null && off == null) {
            return "none";
        }
        if (off == null) {
            return String.format("%s (%dH)", main.getName(), main.getHandsRequired());
        }
        return String.format(
                "%s (%dH), %s (%dH)",
                main.getName(), main.getHandsRequired(),
                off.getName(),  off.getHandsRequired()
        );
    }

    /**
     * Print the stylized title screen with colored ASCII art and menu options.
     */
    private void printTitleScreen() {
        final String RED   = "\u001B[31m";
        final String BLUE  = "\u001B[34m";
        final String RESET = "\u001B[0m";

        System.out.println();


        System.out.println(RED +
                "    __  _______  _   _______________________  _____    " + RESET);
        System.out.println(RED +
                "   /  |/  / __ \\/ | / / ___/_  __/ ____/ __ \\/ ___/    " + RESET);
        System.out.println(RED +
                "  / /|_/ / / / /  |/ /\\__ \\\\ / / / __/ / /_/ /\\__ \\     " + RESET);
        System.out.println(RED +
                " / /  / / /_/ / /|  /___/ // / / /___/ _, _/___/ /     " + RESET);
        System.out.println(RED +
                "/_/  /_/\\____/_/ |_//____//_/ /_____/_/ |_|/____/      " + RESET);


        System.out.println("                    ( _ )                              ");
        System.out.println("                   / __ \\/|                            ");
        System.out.println("                  / /_/  <                             ");


        System.out.println(BLUE +
                "       __  _______\\\\/\\\\___  ___________             " + RESET);
        System.out.println(BLUE +
                "      / / / / ____/ __ \\/ __ \\/ ____/ ___/             " + RESET);
        System.out.println(BLUE +
                "     / /_/ / __/ / /_/ / / / / __/  \\\\__ \\              " + RESET);
        System.out.println(BLUE +
                "    / __  / /___/ _, _/ /_/ / /___ ___/ /              " + RESET);
        System.out.println(BLUE +
                "   /_/ /_/_____/_/ |_|\\____/_____//____/              " + RESET);

        System.out.println();
        System.out.println("1) Play");
        System.out.println("2) Instructions");
        System.out.println("3) Quit");
        System.out.println();
    }

    /**
     * Print a short instructions page, then wait for Enter.
     */
    private void printInstructions() {
        System.out.println("\n=== Instructions ===");
        System.out.println(" - Use W/A/S/D to move around the map.");
        System.out.println(" - I: open party inventory.");
        System.out.println(" - Markets (M) let you buy/sell and equip items.");
        System.out.println(" - Battles may start randomly on common tiles.");
        System.out.println(" - In battle: attack, cast spells, use potions, change equipment.");
        System.out.println("Press Enter to return to the title screen...");
        scanner.nextLine();
    }

    /**
     * Pretty party preview table used before final confirmation.
     */
    private void printPartyPreview(List<Hero> heroes) {
        if (heroes == null || heroes.isEmpty()) {
            System.out.println("  (no heroes selected)");
            return;
        }


        System.out.printf(
                "%-3s %-20s %-10s %-5s %-11s %-11s %-6s %-6s %-6s %-7s %-7s%n",
                "#", "Name", "Class", "Lvl", "HP", "MP",
                "Str", "Dex", "Agi", "Gold", "Exp"
        );


        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            String hpStr = h.getHP() + "/" + h.getBaseHP();
            String mpStr = h.getMP() + "/" + h.getBaseMP();
            String className = h.getClass().getSimpleName();

            System.out.printf(
                    "%-3d %-20s %-10s %-5d %-11s %-11s %-6d %-6d %-6d %-7d %-7d%n",
                    i + 1,
                    h.getName(),
                    className,
                    h.getLevel(),
                    hpStr,
                    mpStr,
                    h.getStrength(),
                    h.getDexterity(),
                    h.getAgility(),
                    h.getGold(),
                    h.getExp()
            );
        }
    }
}
