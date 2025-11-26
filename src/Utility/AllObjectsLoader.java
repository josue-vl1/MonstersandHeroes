package Utility;

import Entities.Hero;
import Entities.Monster;
import Items.Potion;
import Items.Spell;
import Items.Armor;
import Items.Weapon;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that loads all game objects (heroes, monsters, items)
 * from the resource text files and bundles them into a {@link GameData}.
 *
 * This is the single entry point for:
 *  - reading the data files under src/resources/
 *  - delegating parsing to the various *Loader classes
 *  - returning one aggregated GameData object to the rest of the game
 *
 * The class is final and has a private constructor to prevent instantiation:
 * it is used purely via the static {@link #loadAllObjects()} method.
 */
public final class AllObjectsLoader {

    /**
     * Private constructor to prevent instantiation.
     * This is a static utility class.
     */
    private AllObjectsLoader() {
    }

    /**
     * Load all heroes, monsters, armors, weapons, potions and spells
     * from their corresponding resource files.
     *
     * On success, returns a {@link GameData} instance containing
     * lists of all these objects. If an {@link IOException} occurs,
     * the stack trace is printed and the method still returns a
     * GameData object (potentially with some lists empty).</p>
     *
     * @return fully populated GameData object with all loaded entities/items.
     */
    public static GameData loadAllObjects() {
        List<Hero> allHeroes = new ArrayList<>();
        List<Monster> allMonsters = new ArrayList<>();
        List<Armor> allArmors = new ArrayList<>();
        List<Weapon> allWeapons = new ArrayList<>();
        List<Potion> allPotions = new ArrayList<>();
        List<Spell> allSpells = new ArrayList<>();

        try {
            // ----- Heroes -----
            allHeroes.addAll(HeroLoader.loadPaladins(Paths.get("src/resources/Paladins.txt")));
            allHeroes.addAll(HeroLoader.loadWarriors(Paths.get("src/resources/Warriors.txt")));
            allHeroes.addAll(HeroLoader.loadSorcerers(Paths.get("src/resources/Sorcerers.txt")));

            // ----- Monsters -----
            allMonsters.addAll(MonsterLoader.loadDragons(Paths.get("src/resources/Dragons.txt")));
            allMonsters.addAll(MonsterLoader.loadExoskeletons(Paths.get("src/resources/Exoskeletons.txt")));
            allMonsters.addAll(MonsterLoader.loadSpirits(Paths.get("src/resources/Spirits.txt")));

            // ----- Armor -----
            allArmors.addAll(ArmorLoader.loadArmor(Paths.get("src/resources/Armory.txt")));

            // ----- Weapons -----
            allWeapons.addAll(WeaponLoader.loadWeapon(Paths.get("src/resources/Weaponry.txt")));

            // ----- Potions -----
            allPotions.addAll(PotionLoader.loadPotion(Paths.get("src/resources/Potions.txt")));

            // ----- Spells (Fire, Ice, Lightning) -----
            allSpells = SpellLoader.loadAllSpells(
                    Paths.get("src/resources/FireSpells.txt"),
                    Paths.get("src/resources/IceSpells.txt"),
                    Paths.get("src/resources/LightningSpells.txt")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new GameData(allHeroes, allMonsters, allArmors, allWeapons, allPotions, allSpells);
    }
}
