package Utility;

import Entities.Warrior;
import Entities.Paladin;
import Entities.Sorcerer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading hero data (Paladins, Warriors, Sorcerers)
 * from text files.
 *
 * Expected file format for each hero file:
 *   - first line: header (ignored)
 *   - subsequent lines: one hero per line, e.g.
 *
 *     Name   Mana   Strength   Agility   Dexterity   Money   Exp
 *
 * This class uses each hero type's Builder to construct the
 * corresponding {@link Entities.Hero} objects.
 */
public final class HeroLoader {

    /**
     * Private constructor to prevent instantiation.
     * This is a static-only utility class.
     */
    private HeroLoader() { }

    /**
     * Load all Paladins from the given file.
     *
     * @param file path to Paladins.txt
     * @return list of Paladin instances parsed from the file
     * @throws IOException if reading the file fails
     */
    public static List<Paladin> loadPaladins(Path file) throws IOException {
        List<Paladin> paladins = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()){
                    continue;
                }
                String[] parts = line.split("\\s+");
                String name   = parts[0];
                int mana      = Integer.parseInt(parts[1]);
                int strength  = Integer.parseInt(parts[2]);
                int agility   = Integer.parseInt(parts[3]);
                int dexterity = Integer.parseInt(parts[4]);
                int money     = Integer.parseInt(parts[5]);
                int exp       = Integer.parseInt(parts[6]);
                int level = 1;
                int baseHP = level * 100;

                Paladin p = new Paladin.Builder(name, level)
                        .baseHP(baseHP)
                        .baseMP(mana)
                        .strength(strength)
                        .agility(agility)
                        .dexterity(dexterity)
                        .gold(money)
                        .exp(exp)
                        .build();

                paladins.add(p);
            }
        }

        return paladins;
    }

    /**
     * Load all Sorcerers from the given file.
     *
     * @param file path to Sorcerers.txt
     * @return list of Sorcerer instances parsed from the file
     * @throws IOException if reading the file fails
     */
    public static List<Sorcerer> loadSorcerers(Path file) throws IOException {
        List<Sorcerer> sorcerers = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()){
                    continue;
                }

                String[] parts = line.split("\\s+");
                String name   = parts[0];
                int mana      = Integer.parseInt(parts[1]);
                int strength  = Integer.parseInt(parts[2]);
                int agility   = Integer.parseInt(parts[3]);
                int dexterity = Integer.parseInt(parts[4]);
                int money     = Integer.parseInt(parts[5]);
                int exp       = Integer.parseInt(parts[6]);

                int level = 1;
                int baseHP = level * 100;

                Sorcerer s = new Sorcerer.Builder(name, level)
                        .baseHP(baseHP)
                        .baseMP(mana)
                        .strength(strength)
                        .agility(agility)
                        .dexterity(dexterity)
                        .gold(money)
                        .exp(exp)
                        .build();

                sorcerers.add(s);
            }
        }

        return sorcerers;
    }

    /**
     * Load all Warriors from the given file.
     *
     * @param file path to Warriors.txt
     * @return list of Warrior instances parsed from the file
     * @throws IOException if reading the file fails
     */
    public static List<Warrior> loadWarriors(Path file) throws IOException {
        List<Warrior> warriors = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()){
                    continue;
                }
                String[] parts = line.split("\\s+");
                String name   = parts[0];
                int mana      = Integer.parseInt(parts[1]);
                int strength  = Integer.parseInt(parts[2]);
                int agility   = Integer.parseInt(parts[3]);
                int dexterity = Integer.parseInt(parts[4]);
                int money     = Integer.parseInt(parts[5]);
                int exp       = Integer.parseInt(parts[6]);

                int level = 1;
                int baseHP = level * 100;

                Warrior w = new Warrior.Builder(name, level)
                        .baseHP(baseHP)
                        .baseMP(mana)
                        .strength(strength)
                        .agility(agility)
                        .dexterity(dexterity)
                        .gold(money)
                        .exp(exp)
                        .build();

                warriors.add(w);
            }
        }

        return warriors;
    }
}
