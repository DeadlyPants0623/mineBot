package com.example.minebot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class Log {

    public static void sendMessage(String message) {
        LocalPlayer player = Minecraft.getInstance().player;
        Level level = Minecraft.getInstance().level;

        if (player != null && level != null && level.isClientSide) {
            player.displayClientMessage(Component.literal(message), false);
        }
    }
}
