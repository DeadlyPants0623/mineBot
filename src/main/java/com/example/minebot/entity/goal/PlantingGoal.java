package com.example.minebot.entity.goal;

import com.example.minebot.entity.MineBotEntity;
import com.example.minebot.entity.goal.BaseGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class PlantingGoal extends BaseGoal {

    private final Block seedToPlant;  // e.g., Blocks.WHEAT
    private BlockPos targetPos;

    public PlantingGoal(MineBotEntity bot, Block seedToPlant, double speed) {
        super(bot, speed);
        this.seedToPlant = seedToPlant;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    protected boolean shouldStart() {
        return !bot.isInCombat() && findPlantableFarmland(10) != null;
    }

    @Override
    protected boolean shouldContinue() {
        BlockPos nextTarget = findPlantableFarmland(10);
        if (nextTarget != null) {
            targetPos = nextTarget;
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        targetPos = findPlantableFarmland(10);
        if (targetPos != null) {
            moveTo(targetPos.above());
        }
    }

    @Override
    protected void onTick() {
        if (targetPos != null && isCloseEnoughTo(targetPos, 2.0)) {
            plantSeedAt(targetPos);

            targetPos = findPlantableFarmland(10);
            if (targetPos != null) {
                moveTo(targetPos.above());
            }
        }
    }

    private BlockPos findPlantableFarmland(int radius) {
        BlockPos origin = bot.blockPosition();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos farmlandPos = origin.offset(dx, dy, dz);
                    BlockState farmlandState = bot.level().getBlockState(farmlandPos);
                    BlockState aboveState = bot.level().getBlockState(farmlandPos.above());

                    if (farmlandState.is(Blocks.FARMLAND) && aboveState.isAir()) {
                        return farmlandPos;
                    }
                }
            }
        }
        return null;
    }

    private void plantSeedAt(BlockPos farmlandPos) {
        BlockPos cropPos = farmlandPos.above();
        bot.level().setBlock(cropPos, seedToPlant.defaultBlockState(), 3);
    }

    private boolean isCloseEnoughTo(BlockPos pos, double threshold) {
        double dx = bot.getX() - (pos.getX() + 0.5);
        double dy = bot.getY() - (pos.getY() + 0.5);
        double dz = bot.getZ() - (pos.getZ() + 0.5);
        double distanceSq = dx * dx + dy * dy + dz * dz;
        return distanceSq <= threshold * threshold;
    }
}
