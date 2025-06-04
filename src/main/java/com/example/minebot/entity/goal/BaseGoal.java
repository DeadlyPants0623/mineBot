package com.example.minebot.entity.goal;

import com.example.minebot.Log;
import com.example.minebot.entity.MineBotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public abstract class BaseGoal extends Goal {

    protected final MineBotEntity bot;
    protected final double speed;
    protected boolean completed = false;

    public BaseGoal(MineBotEntity bot, double speed) {
        this.bot = bot;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return !completed && shouldStart();
    }

    @Override
    public boolean canContinueToUse() {
        return !completed && shouldContinue();
    }

    @Override
    public void start() {
        Log.sendMessage("Starting: " + this.getClass().getSimpleName());
        onStart();
    }

    @Override
    public void tick() {
        if (bot.level().isClientSide) return;
        onTick();
    }

    /** Called from canUse() */
    protected abstract boolean shouldStart();

    /** Called from canContinueToUse() */
    protected abstract boolean shouldContinue();

    /** Called from start() */
    protected abstract void onStart();

    /** Called from tick() */
    protected abstract void onTick();

    /** Call this when done */
    protected void finish(String reason) {
        Log.sendMessage("Goal finished: " + reason);
        completed = true;
    }

    /** Optional helper */
    protected void moveTo(BlockPos pos) {
        bot.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), speed);
    }
}
