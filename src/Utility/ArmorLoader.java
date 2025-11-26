package Utility;

import Items.Armor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading {@link Armor} objects from a text file.
 *
 * Expected file format (Armory.txt):
 *   - First line: header (ignored)
 *   - Following lines: one armor per line, e.g.
 *       Name   Price   Level   DamageReduction
 *
 * Example:
 *   Platinum_Shield   1200   8   500
 *
 * This class is used by {@link AllObjectsLoader} when building {@link GameData}.
 */
public final class ArmorLoader {

    /**
     * Private constructor to prevent instantiation.
     * This is a static-only utility class.
     */
    private ArmorLoader() { }

    /**
     * Load all armor records from the given file.
     *
     * @param file path to the armor data file (e.g. "src/resources/Armory.txt")
     * @return list of {@link Armor} parsed from the file
     * @throws IOException if reading the file fails
     */
    public static List<Armor> loadArmor(Path file) throws IOException {
        List<Armor> armor = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                String name   = parts[0];
                int price     = Integer.parseInt(parts[1]);
                int level     = Integer.parseInt(parts[2]);
                int damageReduction = Integer.parseInt(parts[3]);

                Armor a = new Armor(name, price, level, damageReduction);
                armor.add(a);
            }
        }

        return armor;
    }
}
