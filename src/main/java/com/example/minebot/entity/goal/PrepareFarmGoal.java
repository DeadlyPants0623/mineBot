package com.example.minebot.entity.goal;

import com.example.minebot.entity.MineBotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class PrepareFarmGoal extends BaseGoal {

    private BlockPos composterPos;
    private BlockPos currentTillTarget;
    private List<BlockPos> farmlandPositions;
    private int tillIndex = 0;
    private int stuckTicks = 0;
    private static final int MAX_STUCK_TICKS = 100;

    public PrepareFarmGoal(MineBotEntity bot, double speed) {
        super(bot, speed);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    protected boolean shouldStart() {
        BlockPos found = findNearbyComposter(10);
        if (found != null) {
            composterPos = found;

            // Skip if all blocks around composter are already farmland
            return hasUntilledSoil(composterPos);
        }
        return false;
    }

    @Override
    protected boolean shouldContinue() {
        return tillIndex < farmlandPositions.size();
    }

    @Override
    protected void onStart() {
        // Place water below composter (if not already there)
        BlockPos waterPos = composterPos.below();
        BlockState currentWater = bot.level().getBlockState(waterPos);
        if (!currentWater.is(Blocks.WATER)) {
            bot.level().setBlock(waterPos, Blocks.WATER.defaultBlockState(), 3);
        }

        // Collect all tillable positions (dirt or grass only)
        farmlandPositions = new ArrayList<>();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockPos groundPos = composterPos.offset(dx, -1, dz);
                BlockState state = bot.level().getBlockState(groundPos);
                if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
                    farmlandPositions.add(groundPos);
                }
            }
        }

        tillIndex = 0;
        if (!farmlandPositions.isEmpty()) {
            currentTillTarget = farmlandPositions.get(tillIndex);
            moveTo(currentTillTarget.above());
        }
    }

    @Override
    protected void onTick() {
        if (currentTillTarget == null) return;

        if (isCloseEnoughTo(currentTillTarget, 2.0)) {
            // Close enough → till the soil
            bot.level().setBlock(currentTillTarget, Blocks.FARMLAND.defaultBlockState(), 3);
            bot.swing(bot.getUsedItemHand());
            tillIndex++;
            stuckTicks = 0;
        } else {
            // Still trying to reach it → increment timeout
            stuckTicks++;
            if (stuckTicks >= MAX_STUCK_TICKS) {
                // Give up on this target
                tillIndex++;
                stuckTicks = 0;
            }
        }

        // Move to next reachable block
        if (tillIndex < farmlandPositions.size()) {
            currentTillTarget = farmlandPositions.get(tillIndex);
            moveTo(currentTillTarget.above());
        }
    }

    private BlockPos findNearbyComposter(int radius) {
        BlockPos origin = bot.blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (bot.level().getBlockState(pos).is(Blocks.COMPOSTER)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasUntilledSoil(BlockPos center) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockPos groundPos = center.offset(dx, -1, dz);
                BlockState state = bot.level().getBlockState(groundPos);
                if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
                    return true; // Needs tilling
                }
            }
        }
        return false; // All already farmland
    }

    private boolean isCloseEnoughTo(BlockPos pos, double threshold) {
        double dx = bot.getX() - (pos.getX() + 0.5);
        double dy = bot.getY() - (pos.getY() + 0.5);
        double dz = bot.getZ() - (pos.getZ() + 0.5);
        return dx * dx + dy * dy + dz * dz <= threshold * threshold;
    }
}
