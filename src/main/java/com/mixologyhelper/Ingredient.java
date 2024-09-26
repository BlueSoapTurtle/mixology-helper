package com.mixologyhelper;

import lombok.Getter;

@Getter
public enum Ingredient {
    MOX(1, "Mox", 45), AGA(2, "Aga", 85), LYE(3, "Lye", 125);

    private final int id;
    private final String name;
    private final int exp;

    Ingredient(int id, String name, int exp) {
        this.id = id;
        this.name = name;
        this.exp = exp;
    }
}
