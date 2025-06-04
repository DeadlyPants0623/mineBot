package com.example.minebot;

import com.example.minebot.entity.MineBotEntity;
import com.example.minebot.entity.ModEntities;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MineBot.MODID)
public class MineBot {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "minebot";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // REQUIRED: public no-arg constructor
    @SuppressWarnings("removal")
    public MineBot() {
        // Register entity types
        ModEntities.register(FMLJavaModLoadingContext.get().getModEventBus());

        // Register any Forge event handlers
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onMobSpawn(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        // Check if it's a hostile mob (you can filter more precisely if needed)
        if (mob instanceof Monster && !(mob instanceof MineBotEntity)) {
            mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE)
                    .setBaseValue(16.0D * 10);
            mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                    mob,
                    MineBotEntity.class,
                    true // must see
            ));
            mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                    mob,
                    Player.class,
                    true // must see
            ));
        }
    }

    @SubscribeEvent
    public static void OnPlayerTick(TickEvent.PlayerTickEvent event) {
    }
}
