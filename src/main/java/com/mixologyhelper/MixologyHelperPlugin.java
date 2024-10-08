package com.mixologyhelper;

import com.google.inject.Provides;
import com.mixologyhelper.data.Process;
import com.mixologyhelper.data.*;
import com.mixologyhelper.ui.BankTooltips;
import com.mixologyhelper.ui.MixologyHelperDebugPanel;
import com.mixologyhelper.ui.MixologyHelperOverlay;
import com.mixologyhelper.ui.MixologyHelperPanel;
import com.mixologyhelper.ui.infobox.BestOrderInfoBox;
import com.mixologyhelper.ui.infobox.OrderFulfilledInfoBox;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.*;

import static com.mixologyhelper.Constants.*;

@Getter
@PluginDescriptor(
        name = "Mixology Helper",
        description = "Helper plugin for the Mastering Mixology minigame"
)
public class MixologyHelperPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ClientThread clientThread;
    @Inject
    private MixologyHelperOverlay overlay;
    @Inject
    private BankTooltips bankTooltips;
    @Inject
    private MixologyHelperPanel panel;
    @Inject
    private MixologyHelperDebugPanel debugPanel;
    @Inject
    private MixologyHelperConfig config;
    @Inject
    private ConfigManager configManager;

    // Infoboxes
    @Inject
    private InfoBoxManager infoBoxManager;
    private OrderFulfilledInfoBox orderFulfilledInfoBox;
    private BestOrderInfoBox bestOrderInfoBox;


    // Player data
    private int moxPaste;
    private int agaPaste;
    private int lyePaste;
    private int moxResin;
    private int agaResin;
    private int lyeResin;
    private int potionsMade;
    private final Map<Ingredient, Integer> bankedPaste = new HashMap<>();


    // Info about current potion in the mixer
    private int ingredient1;
    private int ingredient2;
    private int ingredient3;
    private Recipe currentRecipe = null;


    // The objects
    private final List<TileObject> conveyorBelts = new ArrayList<>();
    private final Map<Ingredient, TileObject> levers = new HashMap<>();
    private final Map<Process, TileObject> machines = new HashMap<>();
    private TileObject mixer;

    // Order list and state
    List<Order> orders = Arrays.asList(new Order(), new Order(), new Order());
    int bestOrderIndex = -1;
    private MixologyStep currentStep = MixologyStep.ADD_INGREDIENT_1;

    @Override
    protected void startUp() {
        overlayManager.add(overlay);
        overlayManager.add(panel);
        overlayManager.add(debugPanel);
        overlayManager.add(bankTooltips);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        overlayManager.remove(panel);
        overlayManager.remove(debugPanel);
        overlayManager.remove(bankTooltips);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState().equals(GameState.LOGGED_IN)) {
            // Reset state on login
            resetState();
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged) {
        // Update order list when herblore level changes
        // This also fixes the best order not updating on login
        if (statChanged.getSkill() == Skill.HERBLORE) {
            updateBestOrder();
        }
    }

    @Subscribe
    protected void onConfigChanged(ConfigChanged configChanged) {
        if (MixologyHelperConfig.GROUP.equals(configChanged.getGroup())) {
            // Remove the arrow if the user disables it
            if (!config.showArrows()) {
                client.clearHintArrow();
            }

            // Update the order list since they might have changed the priority
            clientThread.invokeLater(this::updateBestOrder);
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (event.getContainerId() != InventoryID.BANK.getId()) {
            return;
        }
        ItemContainer bank = event.getItemContainer();
        if (bank == null) {
            return;
        }

        bankedPaste.clear();
        for (Item item : bank.getItems()) {
            Herb herb = Herb.getHerbFromItem(item);
            int pasterPer = 1;
            Ingredient ingredient;
            if (herb == null) {
                ingredient = Ingredient.getIngredientFromItemId(item.getId());
            } else {
                pasterPer = herb.getPastePerHerb();
                ingredient = herb.getIngredient();
            }

            if (ingredient == null) {
                continue;
            }

            int pasteAmount = item.getQuantity() * pasterPer;
            int currentAmount = bankedPaste.getOrDefault(ingredient, 0);
            bankedPaste.put(ingredient, currentAmount + pasteAmount);
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        String message = chatMessage.getMessage();

        if (message.startsWith("You collect some") && message.endsWith("from the mixing vessel.")) {
            // Player picked up the potion from the mixer
            currentStep = MixologyStep.PROCESS_POTION;
        } else if (message.startsWith("You start") && (message.contains("concentrating") || message.contains("homogenising") || message.contains("crystallising"))) {
            // Player started processing the potion
            currentStep = MixologyStep.PROCESSING_POTION;
        } else if (message.startsWith("You finish") && (message.contains("concentrating") || message.contains("homogenising") || message.contains("crystallising"))) {
            // Player finished processing the potion
            currentStep = MixologyStep.DELIVER_POTION;
        } else if (message.startsWith("You deposit some") && (message.contains("and fulfil an order") || message.contains("but fail to fulfil an order"))) {
            // Player delivered the potion successfully
            currentStep = MixologyStep.COMPLETED;
            // Reset for next potion
            resetState();
        }
    }

    private void resetState() {
        currentStep = MixologyStep.ADD_INGREDIENT_1;
        client.clearHintArrow();
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated graphicsObjectCreated) {
        GraphicsObject graphicsObject = graphicsObjectCreated.getGraphicsObject();
        if (graphicsObject.getId() == ObjectID.MATURE_DIGWEED) {
            client.playSoundEffect(DIGWEED_SOUND_EFFECT);
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged varbitChanged) {
        int varbitId = varbitChanged.getVarbitId();
        int value = varbitChanged.getValue();

        if (varbitId == MOX_PASTE_VARBIT) {
            moxPaste = value;
        } else if (varbitId == AGA_PASTE_VARBIT) {
            agaPaste = value;
        } else if (varbitId == LYE_PASTE_VARBIT) {
            lyePaste = value;
        } else if (varbitChanged.getVarpId() == MOX_RESIN_VARBIT) {
            moxResin = value;
        } else if (varbitChanged.getVarpId() == AGA_RESIN_VARBIT) {
            agaResin = value;
        } else if (varbitChanged.getVarpId() == LYE_RESIN_VARBIT) {
            lyeResin = value;
        } else if (varbitChanged.getVarpId() == POTIONS_MADE_VARBIT) {
            potionsMade = value;
            updateInfoboxes();
        } else if (varbitId == INGREDIENT_1_CHANGED) {
            ingredient1 = value;
            updateCurrentStep();
        } else if (varbitId == INGREDIENT_2_CHANGED) {
            ingredient2 = value;
            updateCurrentStep();
        } else if (varbitId == INGREDIENT_3_CHANGED) {
            ingredient3 = value;
            updateCurrentStep();
        } else if (varbitId == RECIPE_VARBIT) {
            currentRecipe = Recipe.getRecipeFromId(value);
            updateCurrentStep();
        } else if (varbitId == ORDER_1_RECIPE_VARBIT && value != 0) {
            updateRecipeFromVarbit(value, 0);
        } else if (varbitId == ORDER_1_PROCESS_VARBIT && value != 0) {
            updateProcessFromVarbit(value, 0);
        } else if (varbitId == ORDER_2_RECIPE_VARBIT && value != 0) {
            updateRecipeFromVarbit(value, 1);
        } else if (varbitId == ORDER_2_PROCESS_VARBIT && value != 0) {
            updateProcessFromVarbit(value, 1);
        } else if (varbitId == ORDER_3_RECIPE_VARBIT && value != 0) {
            updateRecipeFromVarbit(value, 2);
        } else if (varbitId == ORDER_3_PROCESS_VARBIT && value != 0) {
            updateProcessFromVarbit(value, 2);
        }
    }

    @Subscribe
    public void onDecorativeObjectSpawned(DecorativeObjectSpawned event) {
        TileObject tileObject = event.getDecorativeObject();

        if (tileObject.getId() == ObjectID.AGA_LEVER) {
            levers.put(Ingredient.AGA, tileObject);
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        final TileObject tileObject = event.getGameObject();
        if (tileObject.getId() == ObjectID.MATURE_DIGWEED) {
            WorldPoint worldLocation = tileObject.getWorldLocation();
            client.setHintArrow(worldLocation);
        }

        switch (tileObject.getId()) {
            case ObjectID.CONVEYOR_BELT_54917:
                // Only add 2 conveyor belts since there's 2 with the same id
                if (conveyorBelts.size() < 2)
                    conveyorBelts.add(tileObject);
                break;
            case ObjectID.MOX_LEVER:
                levers.put(Ingredient.MOX, tileObject);
                break;
            case 55393:
                levers.put(Ingredient.AGA, tileObject);
                break;
            case ObjectID.LYE_LEVER:
                levers.put(Ingredient.LYE, tileObject);
                break;
            case 55389:
                machines.put(Process.CONCENTRATE, tileObject);
                break;
            case 55390:
                machines.put(Process.HOMOGENISE, tileObject);
                break;
            case 55391:
                machines.put(Process.CRYSTALISE, tileObject);
                break;
            case 55395:
                mixer = tileObject;
                break;
        }
    }

    private void updateRecipeFromVarbit(int value, int i) {
        Recipe recipe = Recipe.getRecipeFromId(value);
        if (recipe != null) {
            orders.get(i).setRecipe(recipe);
        }
        updateBestOrder();
    }

    private void updateProcessFromVarbit(int value, int i) {
        Process process = Process.getProcessFromId(value);
        if (process != null) {
            orders.get(i).setProcess(process);
        }
        updateBestOrder();
    }

    private void updateCurrentStep() {
        // Only update currentStep if it's before PROCESS_POTION
        if (currentStep.ordinal() >= MixologyStep.PROCESS_POTION.ordinal()) {
            return;
        }

        if (ingredient1 == 0) {
            currentStep = MixologyStep.ADD_INGREDIENT_1;
        } else if (ingredient2 == 0) {
            currentStep = MixologyStep.ADD_INGREDIENT_2;
        } else if (ingredient3 == 0) {
            currentStep = MixologyStep.ADD_INGREDIENT_3;
        } else if (currentRecipe != null && correctIngredientsAdded()) {
            currentStep = MixologyStep.PICKUP_POTION;
        } else {
            currentStep = MixologyStep.ADD_INGREDIENT_1; // Reset to first step on incorrect ingredients
        }
    }

    private boolean correctIngredientsAdded() {
        // Check if the correct ingredients are added to avoid progressing on incorrect ones
        return ingredient1 == currentRecipe.getIngredients().get(0).getId() &&
                ingredient2 == currentRecipe.getIngredients().get(1).getId() &&
                ingredient3 == currentRecipe.getIngredients().get(2).getId();
    }

    public void updateBestOrder() {
        // Find the best order based on priority
        bestOrderIndex = -1;
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (client.getRealSkillLevel(Skill.HERBLORE) < order.getRecipe().getLevel()) {
                continue;
            }

            if (bestOrderIndex == -1) {
                bestOrderIndex = i;
                continue;
            }

            if (config.priorityType() == PriorityType.EXPERIENCE) {
                // If same exp, prioritize Concentrate(+20 exp) > Crystalise(+14 exp) > Homogenise(+0 exp)
                if (order.getExp() > orders.get(bestOrderIndex).getExp()) {
                    bestOrderIndex = i;
                }
            } else if (config.priorityType() == PriorityType.INGREDIENT) {
                Ingredient priorityIngredient = config.priorityIngredient();
                int currentOrderIngredientCount = order.getRecipe().getResin(priorityIngredient);
                int bestOrderIngredientCount = orders.get(bestOrderIndex).getRecipe().getResin(priorityIngredient);

                if (currentOrderIngredientCount > bestOrderIngredientCount) {
                    bestOrderIndex = i;
                } else if (currentOrderIngredientCount == bestOrderIngredientCount) {
                    // Tie-breaker: higher experience
                    if (order.getExp() > orders.get(bestOrderIndex).getExp()) {
                        bestOrderIndex = i;
                    }
                }
            }
        }

        updateInfoboxes();
    }

    public Order getbestOrder() {
        if (bestOrderIndex == -1) {
            return null;
        }

        return orders.get(bestOrderIndex);
    }

    public TileObject getLever(Ingredient ingredient) {
        return levers.get(ingredient);
    }

    public TileObject getMachine(Process process) {
        return machines.get(process);
    }

    private void updateInfoboxes() {
        // Setup the orders fulfilled infobox
        if (config.showOrdersFulfilledInfobox()) {
            if (orderFulfilledInfoBox == null) {
                BufferedImage fulfilledImage = ImageUtil.loadImageResource(getClass(), "orders_completed.png");
                orderFulfilledInfoBox = new OrderFulfilledInfoBox(this, fulfilledImage, "Orders Fulfilled", potionsMade);
                infoBoxManager.addInfoBox(orderFulfilledInfoBox);
            }
            orderFulfilledInfoBox.setCount(potionsMade);
        } else if (orderFulfilledInfoBox != null) {
            infoBoxManager.removeInfoBox(orderFulfilledInfoBox);
            orderFulfilledInfoBox = null;
        }

        // Setup the best order infobox
        if (config.showBestOrderInfobox()) {
            Order bestOrder = getbestOrder();
            if (bestOrder != null && bestOrderInfoBox == null) {
                BufferedImage bestOrderImage = ImageUtil.loadImageResource(getClass(), bestOrder.getRecipe().getIcon());
                if (bestOrderImage == null) {
                    bestOrderImage = ImageUtil.loadImageResource(getClass(), "orders_completed.png");
                }
                bestOrderInfoBox = new BestOrderInfoBox(this, bestOrderImage, bestOrder);
                infoBoxManager.addInfoBox(bestOrderInfoBox);
            } else if (bestOrderInfoBox != null) {
                infoBoxManager.removeInfoBox(bestOrderInfoBox);
                bestOrderInfoBox = null;
            }
        }
    }

    @Provides
    MixologyHelperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MixologyHelperConfig.class);
    }
}
