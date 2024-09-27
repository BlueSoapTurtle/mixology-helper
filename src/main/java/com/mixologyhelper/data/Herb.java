package com.mixologyhelper.data;

import lombok.Getter;
import net.runelite.api.Item;
import net.runelite.api.ItemID;

@Getter
public enum Herb {
    MARRENTILL("Marrentill", ItemID.GRIMY_MARRENTILL, ItemID.MARRENTILL, ItemID.MARRENTILL_POTION_UNF, Ingredient.MOX, 13),
    TARROMIN("Tarromin", ItemID.GRIMY_TARROMIN, ItemID.TARROMIN, ItemID.TARROMIN_POTION_UNF, Ingredient.MOX, 15),
    GUAM("Guam leaf", ItemID.GRIMY_GUAM_LEAF, ItemID.GUAM_LEAF, ItemID.GUAM_POTION_UNF, Ingredient.MOX, 10),
    HARRALANDER("Harralander", ItemID.GRIMY_HARRALANDER, ItemID.HARRALANDER, ItemID.HARRALANDER_POTION_UNF, Ingredient.MOX, 20),
    LANTADYME("Lantadyme", ItemID.GRIMY_LANTADYME, ItemID.LANTADYME, ItemID.LANTADYME_POTION_UNF, Ingredient.AGA, 40),
    IRIT("Irit leaf", ItemID.GRIMY_IRIT_LEAF, ItemID.IRIT_LEAF, ItemID.IRIT_POTION_UNF, Ingredient.AGA, 30),
    DWARF("Dwarf weed", ItemID.GRIMY_DWARF_WEED, ItemID.DWARF_WEED, ItemID.DWARF_WEED_POTION_UNF, Ingredient.AGA, 42),
    TORSTOL("Torstol", ItemID.GRIMY_TORSTOL, ItemID.TORSTOL, ItemID.TORSTOL_POTION_UNF, Ingredient.AGA, 44),
    TOADFLAX("Toadflax", ItemID.GRIMY_TOADFLAX, ItemID.TOADFLAX, ItemID.TOADFLAX_POTION_UNF, Ingredient.LYE, 32),
    CADANTINE("Cadantine", ItemID.GRIMY_CADANTINE, ItemID.CADANTINE, ItemID.CADANTINE_POTION_UNF, Ingredient.AGA, 34),
    KWUARM("Kwuarm", ItemID.GRIMY_KWUARM, ItemID.KWUARM, ItemID.KWUARM_POTION_UNF, Ingredient.LYE, 33),
    AVANTOE("Avantoe", ItemID.GRIMY_AVANTOE, ItemID.AVANTOE, ItemID.AVANTOE_POTION_UNF, Ingredient.LYE, 30),
    SNAPDRAGON("Snapdragon", ItemID.GRIMY_SNAPDRAGON, ItemID.SNAPDRAGON, ItemID.SNAPDRAGON_POTION_UNF, Ingredient.LYE, 40),
    RANARR("Ranarr weed", ItemID.GRIMY_RANARR_WEED, ItemID.RANARR_WEED, ItemID.RANARR_POTION_UNF, Ingredient.LYE, 26),
    HUASCA("Huasca", ItemID.GRIMY_HUASCA, ItemID.HUASCA, ItemID.HUASCA_POTION_UNF, Ingredient.AGA, 20),
    ;

    private final String name;
    private final int grimyId;
    private final int cleanId;
    private final int unfId;
    private final Ingredient ingredient;
    private final int pastePerHerb;

    Herb(String name, int grimyId, int cleanId, int unfId, Ingredient ingredient, int pastePerHerb) {
        this.name = name;
        this.grimyId = grimyId;
        this.cleanId = cleanId;
        this.unfId = unfId;
        this.ingredient = ingredient;
        this.pastePerHerb = pastePerHerb;
    }

    public static Herb getHerbFromId(int itemId) {
        for (Herb herb : Herb.values()) {
            if (herb.getGrimyId() == itemId || herb.getCleanId() == itemId || herb.getUnfId() == itemId) {
                return herb;
            }
        }
        return null;
    }

    public static Herb getHerbFromItem(Item item) {
        if (item == null) {
            return null;
        }
        return getHerbFromId(item.getId());
    }
}
