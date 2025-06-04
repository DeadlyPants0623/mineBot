package com.example.minebot.entity.goal.target;

import com.example.minebot.Log;
import com.example.minebot.entity.MineBotEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;

import java.util.List;

public class GetMobsNearPlayerGoal extends TargetGoalBase {

    public GetMobsNearPlayerGoal(Mob mob) {
        super(mob, true, false);
    }

    @Override
    protected LivingEntity findTarget() {
        ServerPlayer player = ((MineBotEntity) mob).getPlayer();
        if (player == null) return null;

        List<LivingEntity> entities = mob.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10));
        for (LivingEntity entity : entities) {
            if (entity instanceof MineBotEntity) continue;
            if (!(entity instanceof Monster)) continue;
            if (!entity.isAlive()) continue;
            Log.sendMessage("Found target entity: " + entity.getName().getString());
            return entity;
        }
        Log.sendMessage("No valid target found near player.");
        return null;
    }
}
