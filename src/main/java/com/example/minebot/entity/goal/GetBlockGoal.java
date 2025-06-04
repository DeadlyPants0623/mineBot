package com.example.minebot.entity.goal;

import com.example.minebot.entity.MineBotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import com.example.minebot.Log;

import java.util.LinkedList;
import java.util.Queue;

public class GetBlockGoal extends BaseGoal {

    private final Block targetBlock;
    private final int totalCount;
    private int remainingCount;
    private BlockPos currentTarget;
    private final Queue<BlockPos> targets = new LinkedList<>();

    public GetBlockGoal(MineBotEntity bot, Block targetBlock, double speed, int amount) {
        super(bot, speed);
        this.targetBlock = targetBlock;
        this.totalCount = amount;
        this.remainingCount = amount;
    }

    @Override
    protected boolean shouldStart() {
        if (remainingCount <= 0) return false;
        targets.clear();
        findNearbyBlocks(15);
        currentTarget = targets.poll();
        return currentTarget != null;
    }

    @Override
    protected boolean shouldContinue() {
        return remainingCount > 0;
    }


    @Override
    protected void onStart() {
        if (currentTarget != null) {
            moveTo(currentTarget);
        }
    }

    @Override
    protected void onTick() {
        // If there's no current target, try to find one
        if (currentTarget == null) {
            findNearbyBlocks(15);
            currentTarget = targets.poll();

            if (currentTarget != null) {
                moveTo(currentTarget);
            }
            return;
        }

        // Revalidate the target block
        if (bot.level().getBlockState(currentTarget).getBlock() != targetBlock) {
            Log.sendMessage("Target block at " + currentTarget + " no longer exists.");
            currentTarget = null; // force re-scan next tick
            return;
        }

        // Continue pursuing current target
        bot.getLookControl().setLookAt(currentTarget.getX(), currentTarget.getY(), currentTarget.getZ());

        if (bot.distanceToSqr(currentTarget.getX(), currentTarget.getY(), currentTarget.getZ()) < 3) {
            if (bot.blockPosition().equals(currentTarget)) {
                moveTo(currentTarget);
                return;
            }

            bot.swing(bot.getUsedItemHand());
            bot.level().destroyBlock(currentTarget, true);
            remainingCount--;
            currentTarget = null; // next tick will find new one
        }

        if (remainingCount <= 0) {
            finish("Mined " + totalCount + " blocks.");
        }
    }

    private void findNearbyBlocks(int radius) {
        BlockPos origin = bot.blockPosition();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (bot.level().getBlockState(pos).getBlock() == targetBlock) {
                        targets.add(pos.immutable());
                        if (targets.size() >= totalCount) return;
                    }
                }
            }
        }
    }
}