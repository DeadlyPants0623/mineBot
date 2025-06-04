package com.example.minebot.entity;

import com.example.minebot.Log;
import com.example.minebot.entity.goal.*;
import com.example.minebot.entity.goal.target.GetMobsNearPlayerGoal;
import com.example.minebot.entity.utils.BotInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;

public class MineBotEntity extends Zombie {

    public boolean isInCombat() {
        return isInCombat;
    }

    public void setInCombat(boolean inCombat) {
        isInCombat = inCombat;
    }

    public double getShouldHealHealth() {
        return shouldHealHealth;
    }

    public void setShouldHealHealth(double shouldHealHealth) {
        this.shouldHealHealth = shouldHealHealth;
    }

    public boolean isNearCampfire() {
        return nearCampfire;
    }

    public void setNearCampfire(boolean nearCampfire) {
        this.nearCampfire = nearCampfire;
    }

    // Hard Variables
    private final BotInventory inventory = new BotInventory(9);
    private double shouldHealHealth = Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).getValue() / 4; // Heal when health is below 25%
    private boolean isInCombat = false;
    private boolean nearCampfire = false;
    public static final Map<UUID, UUID> claimedTargets = new HashMap<>();

    public MineBotEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        Log.sendMessage("MineBot at your service!!");
        this.setCanPickUpLoot(true);
        this.addTag("minebot"); // in MineBotEntity constructor
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.getAvailableGoals().clear();
        this.targetSelector.getAvailableGoals().clear();
//        this.goalSelector.addGoal(1, new FindCampfireForHealingGoal(this, 1.0D));
        this.goalSelector.addGoal(1, new HealGoal(this, 1.0D));
//        this.goalSelector.addGoal(2, new SmartCombatGoal(this, 32.0D));
        this.goalSelector.addGoal(3, new GetMobsNearPlayerGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
//        this.goalSelector.addGoal(4, new FindBedToSleepGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new FollowPlayerGoal(this, 1.0D));
        // Add more goals as needed
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        tryPickupNearbyItems();
        // Wake up if it's day
        if (this.isSleeping() && this.level().getDayTime() < 12000) {
            this.stopSleeping();
            Log.sendMessage("Bot woke up because it's daytime.");
        }
        // Set combat flag based on target proximity
        if (this.getTarget() != null && this.getTarget().isAlive()) {
            double distSq = this.distanceToSqr(this.getTarget());
            this.setInCombat(distSq < 16); // 4 blocks
        } else {
            this.setInCombat(false);
        }

        // Cancel movement while sleeping
        if (this.isSleeping()) {
            this.getNavigation().stop();
            this.setDeltaMovement(0, 0, 0);
        }
    }

    @Override
    public void tick() {
        super.tick();
        // Print inventory contents on key press (client-side check)
        if (net.minecraft.client.Minecraft.getInstance().options.keyInventory.isDown()) {
            Log.sendMessage("Inventory clicked!");
            inventory.printContents();
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            MineBotEntity.claimedTargets.values().removeIf(uuid -> uuid.equals(this.getUUID()));
        }
    }

    public void tryPickupNearbyItems() {
        List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(2));
        for (ItemEntity item : items) {
            if (!item.hasPickUpDelay() && !item.isRemoved()) {
                ItemStack stack = item.getItem();
                ItemStack remaining = inventory.addItem(stack.copy());

                if (remaining.isEmpty()) {
                    item.discard();
                    this.take(item, stack.getCount()); // optional pickup animation
                } else {
                    stack.setCount(remaining.getCount());
                }
            }
        }
    }

    public BotInventory getBotInventory() {
        return inventory;
    }

    /**
     * Get the only player in the world (safe for single-player use)
     */
    public ServerPlayer getPlayer() {
        return this.level().players().stream().filter(p -> p instanceof ServerPlayer).map(p -> (ServerPlayer) p).findFirst().orElse(null);
    }

    @Override
    public boolean isAttackable() {
        // Allow the bot to be attacked
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        // Allow the bot to be collided with
        return true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D);
    }

}
