package com.mixologyhelper;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum Recipe {
    MAMMOTH_MIGHT_MIX(1, "Mammoth-might mix", 60, Arrays.asList(Ingredient.MOX, Ingredient.MOX, Ingredient.MOX)),
    MYSTIC_MANA_AMALGAM(2, "Mystic mana amalgam", 60, Arrays.asList(Ingredient.MOX, Ingredient.MOX, Ingredient.AGA)),
    MARLEYS_MOONLIGHT(3, "Marley's moonlight", 60, Arrays.asList(Ingredient.MOX, Ingredient.MOX, Ingredient.LYE)),
    ALCO_AUGMENTATOR(4, "Alco-augmentator", 76, Arrays.asList(Ingredient.AGA, Ingredient.AGA, Ingredient.AGA)),
    AZURE_AURA_MIX(5, "Azure aura mix", 68, Arrays.asList(Ingredient.AGA, Ingredient.AGA, Ingredient.MOX)),
    AQUALUX_AMALGAM(6, "Aqualux amalgam", 72, Arrays.asList(Ingredient.AGA, Ingredient.LYE, Ingredient.AGA)),
    LIPLACK_LIQUOR(7, "Liplack liquor", 86, Arrays.asList(Ingredient.LYE, Ingredient.LYE, Ingredient.LYE)),
    MEGALITE_LIQUID(8, "Megalite liquid", 80, Arrays.asList(Ingredient.MOX, Ingredient.LYE, Ingredient.LYE)),
    ANTI_LEECH_LOTION(9, "Anti-leech lotion", 84, Arrays.asList(Ingredient.AGA, Ingredient.LYE, Ingredient.LYE)),
    MIXALOT(10, "Mixalot", 64, Arrays.asList(Ingredient.MOX, Ingredient.AGA, Ingredient.LYE)),
    ;

    private final int id; // The id of the recipe from the varbit
    private final String name;
    private final int level;
    private final List<Ingredient> ingredients;

    Recipe(int id, String name, int level, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.ingredients = ingredients;
    }

    public static Recipe getRecipeFromId(int id) {
        for (Recipe recipe : Recipe.values()) {
            if (recipe.getId() == id) {
                return recipe;
            }
        }
        return null;
    }

    public static Recipe getRecipeFromName(String name) {
        // Use regex to remove all tags like <col=6800bf>, <str>, </col>, and </str>
        name = name.replaceAll("<[^>]+>", "");  // This will remove everything within <>

        for (Recipe recipe : Recipe.values()) {
            if (recipe.getName().equalsIgnoreCase(name.trim())) {  // Using trim() to remove extra spaces
                return recipe;
            }
        }
        return null;
    }

    public int getExp() {
        return ingredients.stream().mapToInt(Ingredient::getExp).sum();
    }

    public int getResin(Ingredient ingredient) {
        // Each ingredient is worth 10 resin
        return (int) ingredients.stream().filter(i -> i == ingredient).count() * 10;
    }

    public String getShortName() {
        // Return the first letter of each ingredient, ex MOX, MOX, AGA -> MMA
        StringBuilder shortName = new StringBuilder();
        for (Ingredient ingredient : ingredients) {
            shortName.append(ingredient.getName().charAt(0));
        }
        return shortName.toString();
    }

    public String getIcon() {
        return this.toString().toLowerCase() + ".png";
    }
}
