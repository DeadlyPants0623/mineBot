package com.example.minebot.entity;

import com.example.minebot.MineBot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;

@Mod.EventBusSubscriber(modid = MineBot.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MineBot.MODID);

    public static final ResourceKey<EntityType<?>> MINEBOT_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(MineBot.MODID, "minebot")
            );

    public static final RegistryObject<EntityType<MineBotEntity>> MINEBOT =
            ENTITY_TYPES.register("minebot", () ->
                    EntityType.Builder.of(MineBotEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f)
                            .build(MINEBOT_KEY)
            );

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
