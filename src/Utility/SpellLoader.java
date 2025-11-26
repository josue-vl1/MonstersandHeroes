package Utility;

import Items.FireSpell;
import Items.IceSpell;
import Items.LightningSpell;
import Items.Spell;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading spell data from text files.
 *
 * Supports three concrete spell types:
 *  - {@link FireSpell}
 *  - {@link IceSpell}
 *  - {@link LightningSpell}
 *
 * Expected file format for each spell file:
 *   - first line: header (ignored)
 *   - subsequent lines: one spell per line, e.g.
 *
 *     Name   Price   Level   Damage   ManaCost
 *
 * This class is used by {@link AllObjectsLoader} to build the full spell list
 * inside {@link GameData}.
 */
public final class SpellLoader {

    /**
     * Private constructor to prevent instantiation.
     * This is a static-only utility class.
     */
    private SpellLoader() { }

    /**
     * Load all Fire spells from the given file.
     *
     * @param file path to FireSpells.txt
     * @return list of {@link FireSpell} objects
     * @throws IOException if reading the file fails
     */
    public static List<FireSpell> loadFireSpells(Path file) throws IOException {
        List<FireSpell> spells = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                String name   = parts[0];
                int price     = Integer.parseInt(parts[1]);
                int level     = Integer.parseInt(parts[2]);
                int damage    = Integer.parseInt(parts[3]);
                int manaCost  = Integer.parseInt(parts[4]);

                FireSpell spell = new FireSpell(name, price, level, damage, manaCost);
                spells.add(spell);
            }
        }

        return spells;
    }

    /**
     * Load all Ice spells from the given file.
     *
     * @param file path to IceSpells.txt
     * @return list of {@link IceSpell} objects
     * @throws IOException if reading the file fails
     */
    public static List<IceSpell> loadIceSpells(Path file) throws IOException {
        List<IceSpell> spells = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                String name   = parts[0];
                int price     = Integer.parseInt(parts[1]);
                int level     = Integer.parseInt(parts[2]);
                int damage    = Integer.parseInt(parts[3]);
                int manaCost  = Integer.parseInt(parts[4]);

                IceSpell spell = new IceSpell(name, price, level, damage, manaCost);
                spells.add(spell);
            }
        }

        return spells;
    }

    /**
     * Load all Lightning spells from the given file.
     *
     * @param file path to LightningSpells.txt
     * @return list of {@link LightningSpell} objects
     * @throws IOException if reading the file fails
     */
    public static List<LightningSpell> loadLightningSpells(Path file) throws IOException {
        List<LightningSpell> spells = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                String name   = parts[0];
                int price     = Integer.parseInt(parts[1]);
                int level     = Integer.parseInt(parts[2]);
                int damage    = Integer.parseInt(parts[3]);
                int manaCost  = Integer.parseInt(parts[4]);

                LightningSpell spell = new LightningSpell(name, price, level, damage, manaCost);
                spells.add(spell);
            }
        }

        return spells;
    }

    /**
     * Convenience method that loads Fire, Ice, and Lightning spells
     * from their respective files and returns them as a single list
     * of the abstract type {@link Spell}.
     *
     * @param fireFile       path to FireSpells.txt
     * @param iceFile        path to IceSpells.txt
     * @param lightningFile  path to LightningSpells.txt
     * @return combined list containing all loaded spells
     * @throws IOException if any of the underlying loads fail
     */
    public static List<Spell> loadAllSpells(Path fireFile,
                                            Path iceFile,
                                            Path lightningFile) throws IOException {
        List<Spell> all = new ArrayList<>();
        all.addAll(loadFireSpells(fireFile));
        all.addAll(loadIceSpells(iceFile));
        all.addAll(loadLightningSpells(lightningFile));
        return all;
    }
}
