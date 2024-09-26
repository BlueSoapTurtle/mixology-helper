package com.mixologyhelper;

import net.runelite.client.ui.overlay.infobox.Counter;
import net.runelite.client.util.QuantityFormatter;

import java.awt.image.BufferedImage;

class OrderFulfilledInfoBox extends Counter {
    private final String name;

    OrderFulfilledInfoBox(MixologyHelperPlugin plugin, BufferedImage image, String name, int count) {
        super(image, plugin, count);
        this.name = name;
    }

    @Override
    public String getText() {
        return QuantityFormatter.quantityToRSDecimalStack(getCount());
    }

    @Override
    public String getTooltip() {
        return name;
    }
}

class BestOrderInfoBox extends Counter {
    private final Recipe recipe;

    BestOrderInfoBox(MixologyHelperPlugin plugin, BufferedImage image, Recipe recipe) {
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