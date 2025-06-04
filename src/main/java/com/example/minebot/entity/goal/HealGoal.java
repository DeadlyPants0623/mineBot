package com.example.minebot.entity.goal;

import com.example.minebot.Log;
import com.example.minebot.entity.MineBotEntity;

import java.util.EnumSet;

public class HealGoal extends BaseGoal {

    private final boolean requiresCampfire;

    public  HealGoal(MineBotEntity bot, double speed, boolean requiresCampfire) {
        super(bot, speed);
        this.requiresCampfire = requiresCampfire;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    protected boolean shouldStart() {
        double currentHealth = bot.getHealth();

        if (requiresCampfire && bot.isNearCampfire()) {
            Log.sendMessage("HealGoal skipped: not near a campfire.");
            return false;
        }

        if (bot.isInCombat()) {
            return currentHealth < bot.getShouldHealHealth();
        } else {
            return currentHealth < bot.getMaxHealth();
        }
    }

    @Override
    protected boolean shouldContinue() {
        double currentHealth = bot.getHealth();

        if (requiresCampfire && bot.isNearCampfire()) {
            return false;
        }

        if (bot.isInCombat()) {
            return currentHealth < bot.getShouldHealHealth();
        } else {
            return currentHealth < bot.getMaxHealth();
        }
    }

    @Override
    protected void onStart() {
        Log.sendMessage("HealGoal started.");
    }

    @Override
    protected void onTick() {
        if (bot.level().isClientSide) return;

        double currentHealth = bot.getHealth();
        double targetHealth = bot.isInCombat() ? bot.getShouldHealHealth() : bot.getMaxHealth();

        if (currentHealth < targetHealth) {
            bot.heal(1.0F); // heal rate
            Log.sendMessage("Healing: " + currentHealth + " -> " + (currentHealth + 1.0));
        } else {
            finish("Done healing");
        }
    }
}
