package Utility;

import Entities.Hero;
import Entities.Monster;
import Items.Armor;
import Items.Potion;
import Items.Spell;
import Items.Weapon;

import java.util.List;

/**
 * Simple data holder for all game entities and items loaded from files.
 *
 * This is created by {@link AllObjectsLoader#loadAllObjects()} and then
 * passed into the {@code Game} so it can:
 *  - show available heroes for party selection
 *  - spawn monsters for encounters
 *  - populate markets with armor, weapons, potions, spells, etc.
 */
public final class GameData {

    /**
     * All heroes loaded from the hero data files.
     */
    private List<Hero> heroes;

    /**
     * All monsters loaded from the monster data files.
     */
    private List<Monster> monsters;

    /**
     * All armor items loaded from Armory.txt.
     */
    private List<Armor> armors;

    /**
     * All weapon items loaded from Weaponry.txt.
     */
    private List<Weapon> weapons;

    /**
     * All potion items loaded from Potions.txt.
     */
    private List<Potion> potions;

    /**
     * All spells (Fire, Ice, Lightning) loaded from their data files.
     */
    private List<Spell> spells;

    /**
     * Construct a new GameData container with all lists already loaded.
     *
     * @param heroes   list of all heroes
     * @param monsters list of all monsters
     * @param armors   list of all armor items
     * @param weapons  list of all weapon items
     * @param potions  list of all potions
     * @param spells   list of all spells
     */
    public GameData(List<Hero> heroes,
                    List<Monster> monsters,
                    List<Armor> armors,
                    List<Weapon> weapons,
                    List<Potion> potions,
                    List<Spell> spells) {
        this.heroes = heroes;
        this.monsters = monsters;
        this.armors = armors;
        this.weapons = weapons;
        this.potions = potions;
        this.spells = spells;
    }

    /**
     * @return all heroes available in the game.
     */
    public List<Hero> getHeroes() {
        return heroes;
    }

    /**
     * @return all monsters available in the game.
     */
    public List<Monster> getMonsters() {
        return monsters;
    }

    /**
     * @return all armor items available in the game.
     */
    public List<Armor> getArmors() {
        return armors;
    }

    /**
     * @return all weapon items available in the game.
     */
    public List<Weapon> getWeapons() {
        return weapons;
    }

    /**
     * @return all potion items available in the game.
     */
    public List<Potion> getPotions() {
        return potions;
    }

    /**
     * @return all spells available in the game.
     */
    public List<Spell> getSpells() {
        return spells;
    }
}
