package com.example.minebot.entity.goal;

import com.example.minebot.Log;
import com.example.minebot.entity.MineBotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public class FindCampfireForHealingGoal extends BaseGoal {

    private BlockPos targetCampfire;

    public FindCampfireForHealingGoal(MineBotEntity bot, double speed) {
        super(bot, speed);
    }

    @Override
    protected boolean shouldStart() {
        if (bot.getHealth() >= bot.getShouldHealHealth()) return false;
        targetCampfire = findNearestCampfire(20); // 10 block radius
        return targetCampfire != null;
    }

    @Override
    protected boolean shouldContinue() {
        return targetCampfire != null &&
                bot.distanceToSqr(targetCampfire.getX(), targetCampfire.getY(), targetCampfire.getZ()) > 4.0;
    }

    @Override
    protected void onStart() {
        if (targetCampfire != null) {
//            Log.sendMessage("Found campfire at " + targetCampfire);
            moveTo(targetCampfire.east()); // Stand above the campfire
        }
    }

    @Override
    protected void onTick() {
        if (targetCampfire == null) {
            finish("No campfire to go to");
            return;
        }

        if (bot.distanceToSqr(targetCampfire.getX(), targetCampfire.getY(), targetCampfire.getZ()) <= 4.0) {
            bot.setNearCampfire(true); // optional helper flag
            finish("Reached campfire");
        } else {
            moveTo(targetCampfire.above());
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
}
