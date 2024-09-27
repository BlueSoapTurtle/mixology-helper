package com.mixologyhelper.ui.infobox;

import com.mixologyhelper.MixologyHelperPlugin;
import com.mixologyhelper.data.Order;
import net.runelite.client.ui.overlay.infobox.Counter;

import java.awt.image.BufferedImage;

public class BestOrderInfoBox extends Counter {
    private final Order order;

    public BestOrderInfoBox(MixologyHelperPlugin plugin, BufferedImage image, Order order) {
        super(image, plugin, 1);  // No count since this is the best order
        this.order = order;
    }

    @Override
    public String getTooltip() {
        return order.getRecipe().getName() + "</br>" +
                "Ingredients: " + order.getRecipe().getShortName() + "</br>" +
                "Exp: " + order.getExp();
    }

    @Override
    public String getText() {
        return order.getRecipe().getShortName();
    }
}