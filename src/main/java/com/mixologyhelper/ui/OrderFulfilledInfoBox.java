package com.mixologyhelper.ui;

import com.mixologyhelper.MixologyHelperPlugin;
import net.runelite.client.ui.overlay.infobox.Counter;
import net.runelite.client.util.QuantityFormatter;

import java.awt.image.BufferedImage;

public class OrderFulfilledInfoBox extends Counter {
    private final String name;

    public OrderFulfilledInfoBox(MixologyHelperPlugin plugin, BufferedImage image, String name, int count) {
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