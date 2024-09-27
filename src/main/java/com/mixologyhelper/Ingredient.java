package com.mixologyhelper;

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum Ingredient {
    MOX(1, "Mox", ItemID.MOX_PASTE, 45),
    AGA(2, "Aga", ItemID.MOX_PASTE, 85),
    LYE(3, "Lye", ItemID.MOX_PASTE, 125);

    private final int id;
    private final String name;
    private final int pasteId;
    private final int exp;

    Ingredient(int id, String name, int pasteId, int exp) {
        this.id = id;
        this.name = name;
        this.pasteId = pasteId;
        this.exp = exp;
    }

    public static Ingredient getIngredientFromItemId(int itemId) {
        for (Ingredient ingredient : Ingredient.values()) {
            if (ingredient.getPasteId() == itemId) {
                return ingredient;
            }
        }
        return null;
    }
}
