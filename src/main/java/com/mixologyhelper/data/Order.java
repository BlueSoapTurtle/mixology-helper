package com.mixologyhelper.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order {
    private Recipe recipe;
    private Process process;

    public Order(Recipe recipe, Process process) {
        this.recipe = recipe;
        this.process = process;
    }

    public Order() {
        // Setting default recipe and process, this will be overwritten when we get the order from the server
        this.recipe = Recipe.MAMMOTH_MIGHT_MIX;
        this.process = Process.CONCENTRATE;
    }

    public int getExp() {
        return recipe.getExp() + process.getPotentialExp();
    }

    public int getResin(Ingredient ingredient) {
        return recipe.getResin(ingredient);
    }
}
