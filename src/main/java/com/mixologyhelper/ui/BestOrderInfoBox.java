package com.mixologyhelper.ui;

import com.mixologyhelper.MixologyHelperPlugin;
import com.mixologyhelper.data.Recipe;
import net.runelite.client.ui.overlay.infobox.Counter;

import java.awt.image.BufferedImage;

public class BestOrderInfoBox extends Counter {
    private final Recipe recipe;

    public BestOrderInfoBox(MixologyHelperPlugin plugin, BufferedImage image, Recipe recipe) {
        super(image, plugin, 1);  // No count since this is the best order
        this.recipe = recipe;
    }

    @Override
    public String getTooltip() {
        return recipe.getName() + "</br>" +
                "Ingredients: " + recipe.getShortName() + "</br>" +
                "Exp: " + recipe.getExp();
    }

    @Override
    public String getText() {
        return recipe.getShortName();
    }
}