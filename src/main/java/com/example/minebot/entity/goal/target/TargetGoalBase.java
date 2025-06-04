package com.example.minebot.entity.goal.target;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public abstract class TargetGoalBase extends TargetGoal {

    protected final TargetingConditions targetingConditions;

    public TargetGoalBase(Mob mob, boolean mustSee, boolean mustReach) {
        super(mob, mustSee, mustReach);
        this.targetingConditions = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    }

    protected boolean isValidTarget(LivingEntity entity) {
        return this.canAttack(entity, this.targetingConditions);
    }

    protected ServerLevel getLevel() {
        return (ServerLevel) mob.level();
    }

    // Allow child classes to override how to find a target
    protected abstract LivingEntity findTarget();

    @Override
    public boolean canUse() {
        LivingEntity potentialTarget = findTarget();
        if (potentialTarget == null) return false;

        if (!isValidTarget(potentialTarget)) return false;

        this.targetMob = potentialTarget;
        return true;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.targetMob);
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setTarget(null);
    }
}
