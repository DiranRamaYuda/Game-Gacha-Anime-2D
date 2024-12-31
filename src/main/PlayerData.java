package main;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {
    private int level;
    private int exp;
    private Map<String, Integer> inventory;
    private int totalPower;

    public  PlayerData(int level, int exp, Map<String, Integer> inventory) {
        this.level = level;
        this.exp = exp;
        this.inventory = inventory != null ? inventory : new HashMap<>();
        this.totalPower = calculateTotalPower();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public Map<String, Integer> getInventory() {
        return inventory;
    }

    public void setInventory(Map<String, Integer> inventory) {
        this.inventory = inventory;
        this.totalPower = calculateTotalPower();
    }

    public int getTotalPower() {
        return totalPower;
    }

    public void setTotalPower(int totalPower) {
        this.totalPower = totalPower;
    }

    public void addPower(String character, int power) {
        // Tambahkan karakter ke inventory
        inventory.put(character, inventory.getOrDefault(character, 0));
        // Perbarui totalPower hanya berdasarkan power tambahan
        totalPower += power;
    }

    public int calculateTotalPower() {
        // Reset and recalculate the total power
        int recalculatedTotalPower = 0;
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            String character = entry.getKey();
            String rarity = AnimeRNG.characterRarity.get(character);

            // Null-check: if rarity is null, skip the character
            if (rarity == null) {
                System.out.println("Warning: No rarity found for character: " + character);
                continue;
            }

            Integer power = AnimeRNG.rarityPower.get(rarity);

            // Null-check: if power is null, assume 0 as default
            if (power == null) {
                System.out.println("Warning: No power found for rarity: " + rarity);
                power = 0;
            }

            recalculatedTotalPower += power * entry.getValue();
        }
        return recalculatedTotalPower;
    }
}