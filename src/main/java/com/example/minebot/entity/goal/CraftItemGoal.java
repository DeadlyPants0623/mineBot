package com.example.minebot.entity.goal;

import com.example.minebot.Log;
import com.example.minebot.entity.MineBotEntity;
import com.example.minebot.utils.RecipeRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftItemGoal extends BaseGoal {

    private final Item targetItem;
    private RecipeRegistry.SimpleRecipe matchedRecipe;
    Map<Ingredient, Integer> ingredientCounts = new HashMap<>();

    public CraftItemGoal(MineBotEntity bot, Item targetItem, double speed) {
        super(bot, speed);
        this.targetItem = targetItem;
    }

    @Override
    protected boolean shouldStart() {
        Log.sendMessage("Trying to craft: " + targetItem.getDescriptionId());

        if (!RecipeRegistry.canCraft(targetItem)) {
            Log.sendMessage("No custom recipe for: " + targetItem.getDescriptionId());
            return false;
        }

        matchedRecipe = RecipeRegistry.getRecipe(targetItem).orElse(null);
        if (matchedRecipe == null) return false;

        for (Ingredient ingredient : matchedRecipe.getIngredients()) {
            ingredientCounts.merge(ingredient, 1, Integer::sum);
        }

        List<Ingredient> ingredients = matchedRecipe.getIngredients();

        for (Ingredient ingredient : ingredients) {
            int requiredCount = (int) ingredients.stream().filter(ing -> ing.equals(ingredient)).count();
            if (!bot.getBotInventory().hasIngredient(ingredient, requiredCount)) {
                Log.sendMessage("Missing ingredient: " + requiredCount + "x " + ingredient);
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean shouldContinue() {
        return false; // One-time goal
    }

    @Override
    protected void onStart() {
        if (matchedRecipe != null) {
            List<Ingredient> ingredients = matchedRecipe.getIngredients();
            for (Ingredient ingredient : ingredients) {
                int count = (int) ingredients.stream().filter(ing -> ing.equals(ingredient)).count();
                bot.getBotInventory().consumeIngredient(ingredient, count);
            }

            bot.getBotInventory().addItem(matchedRecipe.getResultCopy());
            Log.sendMessage("Crafted: " + matchedRecipe.getResultCopy().getCount() + "x " + targetItem.getDescriptionId());
        }
        finish("Crafting completed.");
    }

    @Override
    protected void onTick() {
        // Not needed – instant crafting
    }
}
