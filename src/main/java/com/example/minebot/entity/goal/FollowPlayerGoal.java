package com.example.minebot.entity.goal;

import com.example.minebot.Log;
import com.example.minebot.entity.MineBotEntity;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class FollowPlayerGoal extends BaseGoal {

    private final double speed;
    private ServerPlayer player;

    public FollowPlayerGoal(MineBotEntity bot, double speed) {
        super(bot, speed);
        this.speed = speed;
    }

    @Override
    protected boolean shouldStart() {
        updatePlayer();
        boolean result = player != null && player.isAlive();
        Log.sendMessage("Trying to follow player: " + (result ? player.getName().getString() : "null"));
        return result;
    }

    @Override
    protected boolean shouldContinue() {
        updatePlayer();
        boolean result = player != null && player.isAlive();
        Log.sendMessage("Continuing to follow player: " + (result ? player.getName().getString() : "null"));
        return result;
    }

    @Override
    protected void onStart() {
        moveToPlayer();
    }

    @Override
    protected void onTick() {
        moveToPlayer();
    }

    private void updatePlayer() {
        // Get the first available server-side player
        List<ServerPlayer> players = bot.level().players().stream()
                .filter(p -> p instanceof ServerPlayer)
                .map(p -> (ServerPlayer) p)
                .toList();

        player = players.isEmpty() ? null : players.getFirst(); // only one player expected
    }

    private void moveToPlayer() {
        if (player == null || !player.isAlive()) {
            Log.sendMessage("Player is not available for following.");
//            finish("Player not available");
            return;
        }

        double distanceSqr = bot.distanceToSqr(player.getX(), player.getY(), player.getZ());

        if (distanceSqr > 25.0) { // more than 5 blocks
            bot.getNavigation().moveTo(player.getX(), player.getY(), player.getZ(), speed);
        } else if (distanceSqr < 9.0) { // closer than 3 blocks
            bot.getNavigation().stop();
        }
    }
}
