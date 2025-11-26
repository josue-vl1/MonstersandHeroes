package Utility;

import Items.Potion;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading {@link Potion} objects from a text file.
 *
 * Expected file format (Potions.txt):
 *   - first line: header (ignored)
 *   - subsequent lines: one potion per line, e.g.
 *
 *     Name   Price   Level   EffectAmount   AttributeAffected
 *
 * Example:
 *   Health_Potion     250    1   100   Health
 *   Strength_Potion   200    1    75   Strength
 *
 * The {@code attributeAffected} value is interpreted in Battle.handleHeroUsePotion.
 */
public final class PotionLoader {

    /**
     * Private constructor to prevent instantiation.
     * This is a static-only utility class.
     */
    private PotionLoader() { }

    /**
     * Load all potions from the given file.
     *
     * @param file path to the potions data file (e.g. "src/resources/Potions.txt")
     * @return list of {@link Potion} parsed from the file
     * @throws IOException if reading the file fails
     */
    public static List<Potion> loadPotion(Path file) throws IOException {
        List<Potion> potions = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                String name              = parts[0];
                int price                = Integer.parseInt(parts[1]);
                int level                = Integer.parseInt(parts[2]);
                int effectAmount         = Integer.parseInt(parts[3]);
                String attributeAffected = parts[4];

                Potion p = new Potion(name, price, level, effectAmount, attributeAffected);
                potions.add(p);
            }
        }

        return potions;
    }
}
