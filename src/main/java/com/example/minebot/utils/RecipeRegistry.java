package com.example.minebot.utils;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.*;

public class RecipeRegistry {

    // Represents a simple recipe: output + list of ingredients
    public static class SimpleRecipe {
        public final ItemStack result;
        public final List<Ingredient> ingredients;

        public SimpleRecipe(ItemStack result, List<Ingredient> ingredients) {
            this.result = result;
            this.ingredients = ingredients;
        }

        public ItemStack getResultCopy() {
            return result.copy();
        }

        public List<Ingredient> getIngredients() {
            return ingredients;
        }
    }

    private static final Map<Item, SimpleRecipe> RECIPES = new HashMap<>();

    static {
        // Crafting Table = 4 planks -> 1 table
        register(new ItemStack(Items.CRAFTING_TABLE),
                Collections.nCopies(4, Ingredient.of(new ItemLike[]{
                        Items.OAK_PLANKS, Items.SPRUCE_PLANKS, Items.BIRCH_PLANKS,
                        Items.JUNGLE_PLANKS, Items.ACACIA_PLANKS, Items.DARK_OAK_PLANKS
                })));

        // Stick = 2 planks -> 4 sticks
        register(new ItemStack(Items.STICK, 4),
                Collections.nCopies(2, Ingredient.of(new ItemLike[]{
                        Items.OAK_PLANKS, Items.SPRUCE_PLANKS, Items.BIRCH_PLANKS,
                        Items.JUNGLE_PLANKS, Items.ACACIA_PLANKS, Items.DARK_OAK_PLANKS
                })));

        // Furnace = 8 cobblestone -> 1 furnace
        register(new ItemStack(Items.FURNACE),
                Collections.nCopies(8, Ingredient.of(Items.COBBLESTONE)));
    }

    // Registers a custom recipe
    public static void register(ItemStack result, List<Ingredient> ingredients) {
        RECIPES.put(result.getItem(), new SimpleRecipe(result, ingredients));
    }

    // Get a recipe by output item
    public static Optional<SimpleRecipe> getRecipe(Item resultItem) {
        return Optional.ofNullable(RECIPES.get(resultItem));
    }

    // Check if a recipe exists for an item
    public static boolean canCraft(Item resultItem) {
        return RECIPES.containsKey(resultItem);
    }
}
