package com.mixologyhelper;

import net.runelite.api.*;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.QuantityFormatter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class BankTooltips extends Overlay
{
	private final Client client;
	private final MixologyHelperConfig config;
	private final TooltipManager tooltipManager;
	@Inject
	ItemManager itemManager;
	private final StringBuilder itemStringBuilder = new StringBuilder();

	@Inject
	public BankTooltips(Client client, MixologyHelperConfig config, TooltipManager tooltipManager)
	{
		this.client = client;
		this.config = config;
		this.tooltipManager = tooltipManager;

		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (client.isMenuOpen() || !config.showTooltip())
		{
			return null;
		}

		final MenuEntry[] menuEntries = client.getMenuEntries();
		final int last = menuEntries.length - 1;
		if (last < 0)
		{
			return null;
		}

		final MenuEntry menuEntry = menuEntries[last];
		final MenuAction action = menuEntry.getType();
		final int widgetId = menuEntry.getParam1();
		final int groupId = WidgetUtil.componentToInterface(widgetId);

		// Tooltip action type handling
		if (action == MenuAction.CC_OP &&
				(groupId == InterfaceID.INVENTORY || groupId == InterfaceID.BANK || groupId == InterfaceID.BANK_INVENTORY)) {// Make tooltip
			final String text = makeValueTooltip(menuEntry);
			if (text != null) {
				tooltipManager.add(new Tooltip(ColorUtil.prependColorTag(text, new Color(238, 238, 238))));
			}
		}

		return null;
	}

	private String makeValueTooltip(MenuEntry menuEntry)
	{
		// Get the container
		ItemContainer container = getContainer(menuEntry);
		if (container == null)
		{
			return null;
		}

		// Find the item in the container to get stack size
		final int index = menuEntry.getParam0();
		final Item item = container.getItem(index);

		// Get the herb
		Herb herb = Herb.getHerbFromItem(item);
		if (herb == null)
		{
			return null;
		}

		// Get the GE Price of the item if enabled
		int gePrice = 0;
		if (config.showPricePerPaste()) {
			int id = itemManager.canonicalize(item.getId());
			gePrice = itemManager.getItemPrice(id);
		}

		return stackValueText(herb, item.getQuantity(), gePrice);
	}

	private ItemContainer getContainer(MenuEntry menuEntry) {
		final int widgetId = menuEntry.getParam1();

		// Inventory item
		if (widgetId == ComponentID.INVENTORY_CONTAINER || widgetId == ComponentID.BANK_INVENTORY_ITEM_CONTAINER)
		{
			return client.getItemContainer(InventoryID.INVENTORY);
		}

		// Bank item
		else if (widgetId == ComponentID.BANK_ITEM_CONTAINER)
		{
			return client.getItemContainer(InventoryID.BANK);
		}

		return null;
	}

	private String stackValueText(Herb herb, int qty, int gePrice)
	{
		// Append the ingredient name and result amount
		int amount = herb.getResultAmount();
		itemStringBuilder.append(herb.getIngredient().getName() + ": ")
				.append(QuantityFormatter.quantityToStackSize((long) amount * qty));

		// Append the value of each item
		if (qty > 1 && config.showTooltipEach())
		{
			itemStringBuilder.append(" (")
					.append(QuantityFormatter.quantityToStackSize(amount))
					.append(" ea)");
		}

		// Append the price per paste on a new line
		if (gePrice > 0) {
			itemStringBuilder.append("</br>")
					.append(QuantityFormatter.formatNumber(gePrice / amount))
					.append(" gp/paste");
		}

		// Build string and reset builder
		final String text = itemStringBuilder.toString();
		itemStringBuilder.setLength(0);
		return text;
	}
}
