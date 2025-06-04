package com.example.minebot.entity.goal;

import com.example.minebot.Log;
import com.example.minebot.entity.MineBotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class FindBedToSleepGoal extends BaseGoal {

    private BlockPos targetBedPos;

    public FindBedToSleepGoal(MineBotEntity bot, double speed) {
        super(bot, speed);
    }

    @Override
    protected boolean shouldStart() {
        boolean shouldStart = !bot.isSleeping() &&
                bot.level().getDayTime() >= 12000 &&
                bot.level().getDayTime() < 23000 &&
                !bot.isInCombat();
        if (shouldStart)
        {
            Log.sendMessage("FindBedToSleepGoal.shouldStart(): Bot is not sleeping, day time is suitable, and not in combat.");
        }
        return shouldStart;
    }

    @Override
    protected boolean shouldContinue() {
        boolean shouldContinue = !bot.isSleeping() &&
                bot.level().getDayTime() >= 12000 &&
                bot.level().getDayTime() < 23000 &&
                !bot.isInCombat();

        if (shouldContinue)
        {
            Log.sendMessage("FindBedToSleepGoal.shouldStart(): Bot is not sleeping, day time is suitable, and not in combat.");
        }
        return shouldContinue;
    }

    @Override
    protected void onStart() {
        Log.sendMessage("FindBedToSleepGoal.onStart(): Trying to find or place a bed...");

        targetBedPos = findOrPlaceBed();
        if (targetBedPos != null) {
            Log.sendMessage("FindBedToSleepGoal: Moving to bed at " + targetBedPos);
            moveTo(targetBedPos);
        } else {
            Log.sendMessage("FindBedToSleepGoal: Could not find or place a bed. Waiting...");
        }
    }

    @Override
    protected void onTick() {
        if (targetBedPos == null) {
            targetBedPos = findOrPlaceBed();

            if (targetBedPos != null) {
                Log.sendMessage("FindBedToSleepGoal: Got a new bed target. Moving to " + targetBedPos);
                moveTo(targetBedPos);
            }
            return;
        }

        if (bot.blockPosition().distSqr(targetBedPos) <= 2.0 && !bot.isSleeping()) {
            Log.sendMessage("FindBedToSleepGoal: Reached bed at " + targetBedPos + ". Attempting to sleep.");
            bot.startSleeping(targetBedPos);
        }
    }

    private BlockPos findOrPlaceBed() {
        BlockPos origin = bot.blockPosition();

        Log.sendMessage("Scanning for nearby beds...");
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-10, -2, -10), origin.offset(10, 2, 10))) {
            if (bot.level().getBlockState(pos).isBed(bot.level(), pos, bot)) {
                Log.sendMessage("Found existing bed at " + pos);
                return pos;
            }
        }

        Log.sendMessage("No nearby beds found.");
        if (!bot.getBotInventory().contains(Items.RED_BED)) {
            Log.sendMessage("Bot has no bed in inventory. Dropping one.");
            ItemStack bedStack = new ItemStack(Items.RED_BED);
            ItemEntity droppedBed = new ItemEntity(
                    bot.level(),
                    bot.getX(),
                    bot.getY(),
                    bot.getZ(),
                    bedStack
            );
            droppedBed.setPickUpDelay(0);
            bot.level().addFreshEntity(droppedBed);
            return null;
        }

        Log.sendMessage("Bot has a red bed. Searching for placement spot...");
        BlockPos footPos = findFlatGroundNear(origin);
        if (footPos == null) {
            Log.sendMessage("No flat ground found for bed placement.");
            return null;
        }

        Direction facing = bot.getDirection();
        BlockPos headPos = footPos.relative(facing);

        Log.sendMessage("Trying to place bed at: foot=" + footPos + ", head=" + headPos);

        if (!bot.level().getBlockState(footPos).isAir() ||
                !bot.level().getBlockState(headPos).isAir() ||
                !bot.level().getBlockState(footPos.below()).isSolid() ||
                !bot.level().getBlockState(headPos.below()).isSolid()) {
            Log.sendMessage("Cannot place bed: either foot/head is not air or ground is not solid.");
            return null;
        }

        BlockState foot = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.FOOT);
        bot.level().setBlock(footPos, foot, 3);

        BlockState head = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.HEAD);
        bot.level().setBlock(headPos, head, 3);

        bot.getBotInventory().removeItem(Items.RED_BED, 1);
        Log.sendMessage("Placed red bed: foot=" + footPos + ", head=" + headPos + ", facing=" + facing);

        return footPos;
    }

    private BlockPos findFlatGroundNear(BlockPos origin) {
        Log.sendMessage("Looking for flat ground near " + origin);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos pos = origin.offset(dx, 0, dz);
                if (bot.level().getBlockState(pos).isAir() &&
                        bot.level().getBlockState(pos.above()).isAir() &&
                        bot.level().getBlockState(pos.below()).isSolid()) {
                    Log.sendMessage("Found valid placement ground at " + pos);
                    return pos;
                }
            }
        }
        Log.sendMessage("No valid flat ground found.");
        return null;
    }
}
