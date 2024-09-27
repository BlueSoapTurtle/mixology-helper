package com.mixologyhelper.data;

import lombok.Getter;

@Getter
public enum Reward {
    NONE("None", 0, 0, 0),

    APPRENTICE_POTION_PACK("Apprentice Potion Pack", 420, 60, 40),
    ADEPT_POTION_PACK("Adept Potion Pack", 180, 380, 90),
    EXPERT_POTION_PACK("Expert Potion Pack", 430, 280, 590),
    PRESCRIPTION_GOGGLES("Prescription Goggles", 8600, 5600, 11900),
    ALCHEMIST_LABCOAT("Alchemist Labcoat", 2250, 2250, 4750),
    ALCHEMIST_PANTS("Alchemist Pants", 2250, 2250, 4750),  // No resin values provided
    ALCHEMIST_GLOVES("Alchemist Gloves", 2250, 2250, 4750),  // No resin values provided
    REAGENT_POUCH("Reagent Pouch", 16350, 10650, 22600),
    POTION_STORAGE("Potion Storage", 10350, 6700, 14250),
    PRE_POT_DEVICE("Pre-pot Device (Disassembled)", 20700, 13450, 28550),
    ALCHEMIST_AMULET("Alchemist's Amulet", 6900, 4500, 9500),
    ALDARIUM("Aldarium", 90, 60, 120);

    private final String name;
    private final int moxResinCost;
    private final int agaResinCost;
    private final int lyeResinCost;

    Reward(String name, int moxResinCost, int agaResinCost, int lyeResinCost) {
        this.name = name;
        this.moxResinCost = moxResinCost;
        this.agaResinCost = agaResinCost;
        this.lyeResinCost = lyeResinCost;
    }
}