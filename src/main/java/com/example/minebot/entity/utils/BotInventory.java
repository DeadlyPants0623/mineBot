package com.example.minebot.entity.utils;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class BotInventory extends SimpleContainer {
    public BotInventory(int size) {
        super(size);
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.@NotNull Player player) {
        // Always return true to allow the bot to access the inventory
        return true;
    }

    public void printContents() {
        for (int i = 0; i < this.getContainerSize(); i++) {
            if (this.getItem(i).isEmpty()) {
                System.out.println("Slot " + i + ": Empty");
            } else {
                System.out.println("Slot " + i + ": " + this.getItem(i).getCount() + "x " + this.getItem(i).getHoverName().getString());
            }
        }
    }

    public boolean hasIngredient(Ingredient ingredient, int count) {
        int total = 0;
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (ingredient.test(stack)) {
                total += stack.getCount();
                if (total >= count) return true;
            }
        }
        return false;
    }

    public void consumeIngredient(Ingredient ingredient, int count) {
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (!ingredient.test(stack)) continue;

            int remove = Math.min(count, stack.getCount());
            stack.shrink(remove);
            count -= remove;

            if (count <= 0) return;
        }
    }
}
