package com.example.minebot.entity.goal;

import com.example.minebot.Log;
import com.example.minebot.entity.MineBotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;

public class HealGoal extends BaseGoal {

    private BlockPos targetCampfire;

    public HealGoal(MineBotEntity bot, double speed) {
        super(bot, speed);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    protected boolean shouldStart() {
        double currentHealth = bot.getHealth();
        double targetHealth = bot.isInCombat() ? bot.getShouldHealHealth() : bot.getMaxHealth();
        targetCampfire = findNearestCampfire(50);

        return targetCampfire != null && currentHealth < targetHealth;
    }

    @Override
    protected boolean shouldContinue() {
        if (targetCampfire == null) return false;

        double currentHealth = bot.getHealth();
        double targetHealth = bot.isInCombat() ? bot.getShouldHealHealth() : bot.getMaxHealth();

        return currentHealth < targetHealth;
    }

    @Override
    protected void onStart() {
        if (targetCampfire != null) {
            BlockPos nearby = findNearbyStandablePosition(targetCampfire, 3);
            if (nearby != null) {
                moveTo(nearby);
                Log.sendMessage("Moving to nearby standable position at " + nearby);
            } else {
                finish("No nearby standable position found");
                return;
            }
        }
    }

    @Override
    protected void onTick() {
        double currentHealth = bot.getHealth();
        double targetHealth = bot.getMaxHealth();

        if (targetCampfire == null) {
            finish("No campfire available");
            return;
        }

        if (bot.distanceToSqr(targetCampfire.getX(), targetCampfire.getY(), targetCampfire.getZ()) <= 32.0) {
            bot.setNearCampfire(true);
            Log.sendMessage("Reached campfire");
        } else {
            BlockPos nearby = findNearbyStandablePosition(targetCampfire, 3);
            if (nearby != null) {
                moveTo(nearby);
                Log.sendMessage("Moving closer to campfire at " + targetCampfire);
            } else {
                finish("No nearby standable position found");
                return;
            }
            return;
        }

        if (currentHealth < targetHealth) {
            bot.heal(1.0F);
            Log.sendMessage("Healing: " + currentHealth + " -> " + (currentHealth + 1.0));
        } else {
            finish("Done healing");
        }
    }

    private BlockPos findNearestCampfire(int radius) {
        BlockPos origin = bot.blockPosition();
        BlockPos closest = null;
        double minDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -2, -radius), origin.offset(radius, 2, radius))) {
            if (bot.level().getBlockState(pos).is(Blocks.CAMPFIRE)) {
                double dist = pos.distSqr(origin);
                if (dist < minDist) {
                    closest = pos.immutable();
                    minDist = dist;
                }
            }
        }

        return closest;
    }

    private BlockPos findNearbyStandablePosition(BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos candidate = center.offset(dx, 0, dz);
                BlockPos below = candidate.below();

                boolean isAir = bot.level().isEmptyBlock(candidate);
                boolean hasSolidGround = bot.level().getBlockState(below).isSolid();

                if (isAir && hasSolidGround) {
                    return candidate;
                }
            }
        }
        return null;
    }

}
