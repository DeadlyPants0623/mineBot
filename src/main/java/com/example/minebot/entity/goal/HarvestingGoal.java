package com.example.minebot.entity.goal;

import com.example.minebot.entity.MineBotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.EnumSet;

public class HarvestingGoal extends BaseGoal {

    private final Block targetHarvest;
    private BlockPos targetPos;

    public HarvestingGoal(MineBotEntity bot, Block targetTypeToHarvest, double speed) {
        super(bot, speed);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.targetHarvest = targetTypeToHarvest;
    }

    @Override
    protected boolean shouldStart() {
        return !bot.isInCombat() && findNearestHarvestableBlock(targetHarvest, 10) != null;
    }

    @Override
    protected boolean shouldContinue() {
        if (bot.isInCombat()) return false;

        BlockPos nextTarget = findNearestHarvestableBlock(targetHarvest, 10);
        if (nextTarget != null) {
            targetPos = nextTarget;
            return true;
        }

        return false;
    }

    @Override
    protected void onStart() {
        targetPos = findNearestHarvestableBlock(targetHarvest, 10);
        if (targetPos != null) {
            moveTo(targetPos.above());
        }
    }

    @Override
    protected void onTick() {
        if (targetPos != null) {
            if (isCloseEnoughTo(targetPos, 2.0)) {
                performHarvestingAt(targetPos);

                // Find next harvestable block
                targetPos = findNearestHarvestableBlock(targetHarvest, 10);
                if (targetPos != null) {
                    moveTo(targetPos.above());
                }
            }
        }
    }


    private BlockPos findNearestHarvestableBlock(Block blockType, int radius) {
        BlockPos origin = bot.blockPosition();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState state = bot.level().getBlockState(pos);

                    // Check for full-grown crops
                    if (state.getBlock() == blockType) {
                        if (state.hasProperty(BlockStateProperties.AGE_7)) {
                            if (state.getValue(BlockStateProperties.AGE_7) == 7) {
                                return pos;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void performHarvestingAt(BlockPos targetPos) {
        BlockState state = bot.level().getBlockState(targetPos);
        if (state.getBlock() == targetHarvest) {
            bot.level().destroyBlock(targetPos, true);
        }
    }

    private boolean isCloseEnoughTo(BlockPos pos, double threshold) {
        double dx = bot.getX() - (pos.getX() + 0.5);
        double dy = bot.getY() - (pos.getY() + 0.5);
        double dz = bot.getZ() - (pos.getZ() + 0.5);
        double distanceSq = dx * dx + dy * dy + dz * dz;
        return distanceSq <= threshold * threshold;
    }

}
