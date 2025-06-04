package com.example.minebot.entity.utils;

import com.example.minebot.MineBot;
import com.example.minebot.entity.MineBotEntity;
import com.example.minebot.entity.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MineBot.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityAttributes {

    @SubscribeEvent
    public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.MINEBOT.get(), MineBotEntity.createAttributes().build());
    }
}
