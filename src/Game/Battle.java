package Game;

import Entities.Hero;
import Entities.Monster;
import Items.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Handles one battle between the party and a group of monsters.
 * Single responsibility: run the battle loop and resolve the outcome
 * (heroes win or lose), including:
 *     Processing heroes' actions (attack, spell, potion, equipment).</li>
 *     Processing monsters' attacks.</li>
 *     Applying damage, dodge, spell debuffs, and end-of-round regen.</li>
 *     Granting rewards or handling defeat when the battle ends.</li>
 */
public class Battle {

    /**
     * The party of heroes participating in this battle.
     */
    private final Party party;

    /**
     * The monsters in this battle.
     * This list is mutable in terms of HP and stats, but we keep
     * the same Monster objects throughout the battle.
     */
    private final List<Monster> monsters;

    /**
     * Scanner used to read player input during the battle.
     */
    private final Scanner scanner;

    /**
     * Random number generator for dodge checks, target selection, etc.
     */
    private final Random random = new Random();

    /**
     * Percentage used for spell debuffs (e.g., 0.20 = 20% reduction).
     */
    private static final double spellDebuff = 0.20;

    /**
     * Construct a new Battle with the given party, monsters, and Scanner.
     * Monsters are reset for battle (HP and stats), while heroes start
     * with whatever HP/MP they had when the battle was triggered.
     *
     * @param party    the heroes' party
     * @param monsters the monsters to fight
     * @param scanner  input source for player decisions
     */
    public Battle(Party party, List<Monster> monsters, Scanner scanner) {
        this.party = party;
        this.monsters = new ArrayList<>(monsters);
        this.scanner = scanner;
        for (Monster m : this.monsters) {
            m.resetForBattle();
        }
    }

    /**
     * Run the battle until either all monsters or all heroes are down.
     *
     * @return true if heroes win, false if monsters win.
     */
    public boolean run() {
        System.out.println("\n=== A battle begins! ===");
        printStatus();

        while (hasLivingHeroes() && hasLivingMonsters()) {
            heroesTurn();
            if (!hasLivingMonsters()) {
                break;
            }

            monstersTurn();
            endOfRoundRegeneration();
            printStatus();
        }

        if (hasLivingHeroes()) {
            handleHeroesWin();
            return true;
        } else {
            handleHeroesLose();
            return false;
        }
    }

    // ---------- Turn logic ----------

    /**
     * Process one "heroes' turn", giving each living hero a chance
     * to choose an action (attack, spell, potion, change equipment, etc.).
     */
    private void heroesTurn() {
        System.out.println("\n--- Heroes' turn ---");

        List<Hero> heroes = party.getMembers();
        for (Hero hero : heroes) {
            if (hero.isFainted()) {
                continue;
            }

            if (!hasLivingMonsters()) {
                return;
            }

            boolean done = false;
            while (!done) {
                System.out.printf(
                        "%n%s's turn (HP: %d/%d)%n",
                        hero.getName(), hero.getHP(), hero.getBaseHP()
                );
                System.out.println("Choose action:");
                System.out.println("1) Attack");
                System.out.println("2) Cast spell");
                System.out.println("3) Use potion");
                System.out.println("4) Change equipment");
                System.out.println("5) Show heroes' stats");
                System.out.println("6) Show monsters' stats");
                System.out.println("0) Skip action");

                int choice = readIntInRange("Your choice: ", 0, 6);
                switch (choice) {
                    case 1:
                        handleHeroAttack(hero);
                        done = true;
                        break;
                    case 2:
                        if (handleHeroCastSpell(hero)) {
                            done = true;
                        }
                        break;
                    case 3:
                        if (handleHeroUsePotion(hero)) {
                            done = true;
                        }
                        break;
                    case 4:
                        if (changeEquipment(hero)) {
                            done = true;
                        }
                        break;
                    case 5:
                        showHeroesStats();
                        break;
                    case 6:
                        showMonstersStats();
                        break;
                    case 0:
                        System.out.println(hero.getName() + " skips their action.");
                        done = true;
                        break;
                }
            }
        }
    }

    /**
     * Process one "monsters' turn", where each living monster attacks
     * a random living hero (if any remain).
     */
    private void monstersTurn() {
        System.out.println("\n--- Monsters' turn ---");

        List<Hero> heroes = party.getMembers();
        List<Hero> livingHeroes = livingHeroes(heroes);

        for (Monster m : monsters) {
            if (m.isDead()) {
                continue;
            }

            if (livingHeroes.isEmpty()) {
                return;
            }
            Hero target = livingHeroes.get(random.nextInt(livingHeroes.size()));
            double dodgeChance = target.getAgility() * 0.0005;
            dodgeChance = Math.min(0.5, dodgeChance);

            if (random.nextDouble() < dodgeChance) {
                System.out.printf("%s dodged the attack from %s!%n",
                        target.getName(), m.getName());
                continue;
            }

            int damage = computeMonsterAttackDamage(m, target);
            System.out.printf("%s attacks %s for %d damage.%n",
                    m.getName(), target.getName(), damage);
            target.takeDamage(damage);

            if (target.isFainted()) {
                System.out.printf("%s has fainted!%n", target.getName());
                livingHeroes = livingHeroes(heroes);
            }
        }
    }

    // ---------- Actions ----------

    /**
     * Handle a normal physical attack from the given hero.
     * The hero chooses a living monster to attack, and the monster
     * may dodge based on its dodge stat.
     */
    private void handleHeroAttack(Hero hero) {
        List<Monster> living = livingMonsters();

        System.out.println("\nChoose a monster to attack:");
        for (int i = 0; i < living.size(); i++) {
            Monster m = living.get(i);
            System.out.printf(
                    "%d) %s (HP: %d/%d, dmg: %d, def: %d, dodge: %d%%)%n",
                    i + 1,
                    m.getName(),
                    m.getHP(), m.getBaseHP(),
                    m.getBaseDamage(),
                    m.getDefense(),
                    m.getDodge()
            );
        }

        int choice = readIntInRange("Target (0 to cancel): ", 0, living.size());
        if (choice == 0) {
            System.out.println(hero.getName() + " cancels the attack.");
            return;
        }

        Monster target = living.get(choice - 1);
        double dodgeChance = target.getDodge() / 100.0;
        dodgeChance = Math.min(0.4, dodgeChance);

        if (random.nextDouble() < dodgeChance) {
            System.out.printf("%s dodged the attack!%n", target.getName());
            return;
        }

        int damage = computeHeroAttackDamage(hero, target);

        System.out.printf("%s attacks %s for %d damage.%n",
                hero.getName(), target.getName(), damage);
        target.takeDamage(damage);

        if (target.isDead()) {
            System.out.printf("%s has been defeated!%n", target.getName());
        }
    }

    /**
     * Handle a hero casting a spell:

     *     Choose a spell from inventory.
     *     Choose a monster target.
     *     Spend MP, apply damage and elemental debuff.
     *     Remove the spell (one-use) from inventory.
     *
     *
     * @return true if a spell was successfully cast (turn consumed),
     * false if nothing happened (no turn consumed).
     */
    private boolean handleHeroCastSpell(Hero hero) {

        List<Item> allItems = hero.getInventory().getItems();
        List<Spell> spells = new ArrayList<>();
        for (Item it : allItems) {
            if (it instanceof Spell) {
                spells.add((Spell) it);
            }
        }

        if (spells.isEmpty()) {
            System.out.println("No spells in inventory.");
            return false;
        }

        System.out.println("\n=== Spells in inventory ===");
        System.out.printf(
                "%-3s %-20s %-5s %-8s %-8s%n",
                "#", "Name", "Lvl", "Damage", "MPcost"
        );

        for (int i = 0; i < spells.size(); i++) {
            Spell s = spells.get(i);
            System.out.printf(
                    "%-3d %-20s %-5d %-8d %-8d%n",
                    i + 1,
                    s.getName(),
                    s.getLevel(),
                    s.getDamage(),
                    s.getManaCost()
            );
        }

        System.out.println("Enter the number of the spell to cast, or 0 to cancel.");
        int spellChoice = readIntInRange("Your choice: ", 0, spells.size());
        if (spellChoice == 0) {
            return false;
        }

        Spell chosen = spells.get(spellChoice - 1);
        List<Monster> living = livingMonsters();
        if (living.isEmpty()) {
            System.out.println("There are no monsters left to target.");
            return false;
        }

        System.out.println("\nChoose a monster to target:");
        for (int i = 0; i < living.size(); i++) {
            Monster m = living.get(i);
            System.out.printf(
                    "%d) %s (HP: %d/%d, dmg: %d, def: %d, dodge: %d%%)%n",
                    i + 1,
                    m.getName(),
                    m.getHP(), m.getBaseHP(),
                    m.getBaseDamage(),
                    m.getDefense(),
                    m.getDodge()
            );
        }

        int targetChoice = readIntInRange("Target (0 to cancel): ", 0, living.size());
        if (targetChoice == 0) {
            return false;
        }

        Monster target = living.get(targetChoice - 1);
        int mpCost = chosen.getManaCost();
        if (!hero.spendMP(mpCost)) {

            System.out.println("Not enough MP to cast this spell.");
            return false;
        }

        double spellDodgeChance = (target.getDodge() / 100.0) * 0.5;
        spellDodgeChance = Math.min(0.30, spellDodgeChance);

        if (random.nextDouble() < spellDodgeChance) {
            System.out.printf("%s dodged the spell from %s!%n",
                    target.getName(), hero.getName());
            hero.getInventory().removeItem(chosen);
            return true;
        }

        int damage = computeSpellDamage(hero, chosen);
        System.out.printf("%s casts %s on %s for %d damage.%n",
                hero.getName(), chosen.getName(), target.getName(), damage);
        target.takeDamage(damage);

        if (chosen instanceof FireSpell) {
            target.reduceDefensePercent(spellDebuff);
            System.out.println(target.getName() + "'s defense has been reduced!");
        } else if (chosen instanceof IceSpell) {
            target.reduceDamagePercent(spellDebuff);
            System.out.println(target.getName() + "'s damage has been reduced!");
        } else if (chosen instanceof LightningSpell) {
            target.reduceDodgePercent(spellDebuff);
            System.out.println(target.getName() + "'s dodge chance has been reduced!");
        }

        hero.getInventory().removeItem(chosen);
        if (target.isDead()) {
            System.out.printf("%s has been defeated by the spell!%n", target.getName());
        }
        return true;
    }

    /**
     * Handle a hero using a potion from their inventory.
     * Potions can affect HP, MP, strength, dexterity, agility, or all stats,
     * depending on the "attributeAffected" string from the data file.
     *
     * @return true if a potion was used (turn consumed), false if not.
     */
    private boolean handleHeroUsePotion(Hero hero) {
        List<Item> allItems = hero.getInventory().getItems();
        List<Potion> potions = new ArrayList<>();
        for (Item it : allItems) {
            if (it instanceof Potion) {
                potions.add((Potion) it);
            }
        }

        if (potions.isEmpty()) {
            System.out.println("No potions in inventory.");
            return false;
        }
        System.out.println("\n=== Potions in inventory ===");
        System.out.printf(
                "%-3s %-20s %-5s %-8s %-12s%n",
                "#", "Name", "Lvl", "Amount", "Affects"
        );

        for (int i = 0; i < potions.size(); i++) {
            Potion p = potions.get(i);
            System.out.printf(
                    "%-3d %-20s %-5d %-8d %-12s%n",
                    i + 1,
                    p.getName(),
                    p.getLevel(),
                    p.getEffectIncrease(),
                    p.getAttributeAffected()
            );
        }

        System.out.println("Enter the number of the potion to use, or 0 to cancel.");
        int choice = readIntInRange("Your choice: ", 0, potions.size());
        if (choice == 0) {
            return false;
        }

        Potion chosen = potions.get(choice - 1);
        int amount = chosen.getEffectIncrease();
        String attr = chosen.getAttributeAffected();
        String attrLower = attr.toLowerCase();

        boolean used = false;

        if (attrLower.contains("health") || attrLower.contains("hp")) {
            hero.addHP(amount);
            System.out.printf("%s uses %s and gains %d HP (now %d HP).%n",
                    hero.getName(), chosen.getName(), amount, hero.getHP());
            used = true;
        }

        if (attrLower.contains("mana") || attrLower.contains("mp")) {
            hero.addMP(amount);
            System.out.printf("%s gains %d MP (now %d MP).%n",
                    hero.getName(), amount, hero.getMP());
            used = true;
        }

        if (attrLower.contains("strength")) {
            hero.addStrength(amount);
            System.out.printf("%s's Strength increased by %d (now %d).%n",
                    hero.getName(), amount, hero.getStrength());
            used = true;
        }

        if (attrLower.contains("dexterity")) {
            hero.addDexterity(amount);
            System.out.printf("%s's Dexterity increased by %d (now %d).%n",
                    hero.getName(), amount, hero.getDexterity());
            used = true;
        }

        if (attrLower.contains("agility")) {
            hero.addAgility(amount);
            System.out.printf("%s's Agility increased by %d (now %d).%n",
                    hero.getName(), amount, hero.getAgility());
            used = true;
        }

        if (attrLower.equals("all")) {
            hero.addHP(amount);
            hero.addMP(amount);
            hero.addStrength(amount);
            hero.addDexterity(amount);
            hero.addAgility(amount);
            System.out.printf(
                    "%s uses %s and increases all stats by %d!%n",
                    hero.getName(), chosen.getName(), amount
            );
            used = true;
        }

        if (!used) {
            System.out.println("Potion has unknown effect type: " + attr);
            return false;
        }

        hero.getInventory().removeItem(chosen);
        return true;
    }

    // ---------- Damage formulas (simple, extendable) ----------

    /**
     * Compute physical attack damage from a hero to a monster.
     * Uses hero strength + total weapon damage and reduces
     * damage based on the monster's defense.
     */
    private int computeHeroAttackDamage(Hero hero, Monster target) {
        int strength = hero.getStrength();
        int weaponDamage = hero.getEquippedWeaponDamage();
        double base = (strength + weaponDamage) * 0.05;

        int def = target.getDefense();
        double defenseFactor = 1.0 + def / 300.0;
        int reduced = (int) Math.round(base / defenseFactor);

        return Math.max(1, reduced);
    }

    /**
     * Compute physical attack damage from a monster to a hero.
     * Uses the monster's base damage, reduced by the hero's armor.
     */
    private int computeMonsterAttackDamage(Monster monster, Hero target) {
        double raw = monster.getBaseDamage() * 0.05;
        int armor = target.getEquippedArmorReduction();
        double defenseFactor = 1.0 + armor / 300.0;
        double reduced = raw / defenseFactor;
        int dmg = (int) Math.round(reduced);
        return Math.max(1, dmg);
    }

    /**
     * Compute spell damage based on the spell's base damage and the hero's
     * dexterity (spell power).
     */
    private int computeSpellDamage(Hero hero, Spell spell) {
        int spellBase = spell.getDamage();
        int dex       = hero.getDexterity();
        double factor = 1.0 + (dex / 10000.0);
        int dmg = (int) Math.round(spellBase * factor);
        return Math.max(1, dmg);
    }

    // ---------- Round end & outcome ----------

    /**
     * At the end of each round, all heroes regenerate a portion of HP/MP.
     */
    private void endOfRoundRegeneration() {
        System.out.println("\nEnd of round: heroes regain some HP/MP.");
        for (Hero h : party.getMembers()) {
            h.regenAfterRound();
        }
    }

    /**
     * Handle the heroes winning the battle:
     *     Non-fainted heroes gain gold and experience.
     *     Fainted heroes are revived with partial HP/MP but gain nothing.
     * Rewards are scaled by the monsters' levels.
     */
    private void handleHeroesWin() {
        System.out.println("\n=== Heroes win the battle! ===");

        int maxMonsterLevel = 0;
        int totalMonsterLevel = 0;

        for (Monster m : monsters) {
            int lvl = m.getLevel();
            totalMonsterLevel += lvl;
            if (lvl > maxMonsterLevel) {
                maxMonsterLevel = lvl;
            }
        }

        for (Hero h : party.getMembers()) {
            if (h.isFainted()) {
                h.reviveAfterBattle();
                System.out.printf(
                        "%s is revived with partial HP/MP but gains no rewards.%n",
                        h.getName()
                );
            } else {
                int goldGain = maxMonsterLevel * 100;
                int expGain  = totalMonsterLevel;

                h.addGold(goldGain);
                h.gainExperience(expGain);

                System.out.printf(
                        "%s gains %d gold and %d exp.%n",
                        h.getName(), goldGain, expGain
                );
            }
        }
    }

    /**
     * Handle the heroes losing the battle.
     * For now this just prints a message; you could extend it to
     * do more (e.g., send heroes back to a respawn point).
     */
    private void handleHeroesLose() {
        System.out.println("\n=== The party has been defeated... ===");
        System.out.println("Game over.");
    }

    // ---------- Status & helpers ----------

    /**
     * @return true if at least one hero is still alive.
     */
    private boolean hasLivingHeroes() {
        for (Hero h : party.getMembers()) {
            if (!h.isFainted()) return true;
        }
        return false;
    }

    /**
     * @return true if at least one monster is still alive.
     */
    private boolean hasLivingMonsters() {
        for (Monster m : monsters) {
            if (!m.isDead()) return true;
        }
        return false;
    }

    /**
     * Filter the given hero list to only living heroes.
     */
    private List<Hero> livingHeroes(List<Hero> heroes) {
        List<Hero> result = new ArrayList<>();
        for (Hero h : heroes) {
            if (!h.isFainted()) {
                result.add(h);
            }
        }
        return result;
    }

    /**
     * @return a list of all living monsters in this battle.
     */
    private List<Monster> livingMonsters() {
        List<Monster> result = new ArrayList<>();
        for (Monster m : monsters) {
            if (!m.isDead()) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * Print a short status summary of heroes and monsters
     * (name, level, HP) during the battle.
     */
    private void printStatus() {
        System.out.println("\n--- Battle status ---");

        System.out.println("Heroes:");
        System.out.printf(
                "%-3s %-15s %-7s %-10s%n",
                "#", "Name", "Level", "HP"
        );
        List<Hero> heroes = party.getMembers();
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            System.out.printf(
                    "%-3d %-15s %-7d %-10s%n",
                    i + 1,
                    h.getName(),
                    h.getLevel(),
                    h.getHP() + "/" + h.getBaseHP()
            );
        }

        System.out.println("\nMonsters:");
        System.out.printf(
                "%-3s %-15s %-7s %-10s%n",
                "#", "Name", "Level", "HP"
        );
        List<Monster> aliveMonsters = livingMonsters();
        for (int i = 0; i < aliveMonsters.size(); i++) {
            Monster m = aliveMonsters.get(i);
            System.out.printf(
                    "%-3d %-15s %-7d %-10s%n",
                    i + 1,
                    m.getName(),
                    m.getLevel(),
                    m.getHP() + "/" + m.getBaseHP()
            );
        }
    }

    /**
     * Show a detailed table of all heroes' stats.
     * This can be called during the heroes' turn without consuming
     * their action (by design of the menu).
     */
    private void showHeroesStats() {
        System.out.println("\n=== Heroes' Detailed Stats ===");
        System.out.printf(
                "%-3s %-20s %-5s %-11s %-11s %-6s %-6s %-6s %-7s %-7s%n",
                "#", "Name", "Lvl", "HP", "MP", "Str", "Dex", "Agi", "Gold", "Exp"
        );

        List<Hero> heroes = party.getMembers();

        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            String hpStr = h.getHP() + "/" + h.getBaseHP();
            String mpStr = h.getMP() + "/" + h.getBaseMP();

            System.out.printf(
                    "%-3d %-20s %-5d %-11s %-11s %-6d %-6d %-6d %-7d %-7d%n",
                    i + 1,
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
    }

    /**
     * Show a detailed table of all monsters' stats.
     * Display includes current HP, base damage, defense, and dodge chance.
     */
    private void showMonstersStats() {
        System.out.println("\n=== Monsters' Detailed Stats ===");
        System.out.printf(
                "%-3s %-15s %-5s %-10s %-10s %-10s %-8s%n",
                "#", "Name", "Lvl", "HP", "BaseDmg", "Defense", "Dodge%"
        );

        List<Monster> all = this.monsters;
        for (int i = 0; i < all.size(); i++) {
            Monster m = all.get(i);
            String hpStr = m.getHP() + "/" + m.getBaseHP();

            System.out.printf(
                    "%-3d %-15s %-5d %-10s %-10d %-10d %-8d%n",
                    i + 1,
                    m.getName(),
                    m.getLevel(),
                    hpStr,
                    m.getBaseDamage(),
                    m.getDefense(),
                    m.getDodge()
            );
        }
    }

    /**
     * Equipment sub-menu during battle for a given hero.
     * The player can change weapon, change armor, or go back.
     *
     * @return true if any equipment was changed (turn consumed),
     * false otherwise.
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
     * Weapon-changing logic: list all weapons in hero's inventory,
     * let the player pick one, and equip it (respecting hand rules).
     *
     * @return true if a weapon was equipped, false if cancelled.
     */
    private boolean changeWeapon(Hero hero) {
        List<Item> all = hero.getInventory().getItems();
        List<Weapon> weapons = new ArrayList<>();
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
     * Armor-changing logic: list all armor items in hero's inventory
     * and equip the selected one.
     *
     * @return true if armor was equipped, false if cancelled.
     */
    private boolean changeArmor(Hero hero) {
        List<Item> all = hero.getInventory().getItems();
        List<Armor> armors = new ArrayList<>();
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
     * Helper to describe the hero's equipped weapons, including whether
     * each is one-handed or two-handed.
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
     * Read an integer from the user, ensuring it lies within [min, max].
     * Keeps prompting until a valid integer in range is entered.
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
