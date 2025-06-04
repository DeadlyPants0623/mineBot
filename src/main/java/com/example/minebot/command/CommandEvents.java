package com.example.minebot.command;

import com.example.minebot.MineBot;
import com.example.minebot.entity.MineBotEntity;
import com.example.minebot.entity.ModEntities;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MineBot.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("minebot").then(Commands.literal("spawn").executes(ctx -> {
            try {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                ServerLevel level = player.serverLevel();

                System.out.println("Spawning bot...");

                // Spawn custom MineBotEntity
                MineBotEntity bot = new MineBotEntity(ModEntities.MINEBOT.get(), level);
                bot.setPos(player.getX() + 5, player.getY(), player.getZ());
                bot.setCustomName(Component.literal("MineBot"));
                bot.setPersistenceRequired(); // So it doesn't despawn

                level.addFreshEntity(bot);

                return 1;
            } catch (Exception e) {
                e.printStackTrace(); // Prints in console
                ctx.getSource().sendFailure(Component.literal("Failed to spawn MineBotEntity: " + e));
                return 0;
            }
        })));
    }
}

