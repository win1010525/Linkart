package com.github.vini2003.linkart.mixin;

import com.github.vini2003.linkart.Linkart;
import com.github.vini2003.linkart.utility.CartOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    private CartOperation operation;

    @Inject(at = @At("HEAD"), method = "interact", cancellable = true)
    void onInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (entity instanceof AbstractMinecartEntity minecart) {
            if (getWorld().isClient()) return;

            PlayerEntity player = (PlayerEntity) (Object) this;
            ItemStack stack = player.getStackInHand(hand);

            if (!stack.isIn(Linkart.LINKERS)) return;

            if (this.operation != null) {
                if (this.operation.minecart() != null && this.operation.minecart() != minecart &&
                        minecart.isAlive() && this.operation.minecart().isAlive()) {
                    var result = this.operation.operation().perform(minecart, this.operation, Optional.of(player), stack);
                    finishOperation(cir, minecart, result);
                } else {
                    finishOperation(cir, minecart, ActionResult.FAIL);
                }
                this.operation = null;
            } else if (minecart.linkart$getFollower() != null) {
                this.operation = new CartOperation(CartOperation.Operation.UNLINKING, minecart);
                finishOperation(cir, minecart, ActionResult.SUCCESS);
            } else {
                this.operation = new CartOperation(CartOperation.Operation.LINKING, minecart);
                finishOperation(cir, minecart, ActionResult.SUCCESS);
            }
        }
    }

    @Unique
    private void finishOperation(CallbackInfoReturnable<ActionResult> cir, AbstractMinecartEntity minecart, ActionResult result) {
        if (result.isAccepted()) {
            ((ServerWorld) minecart.getWorld()).spawnParticles(ParticleTypes.HAPPY_VILLAGER, minecart.getX(), minecart.getY() + 0.2, minecart.getZ(), 10, 0.5, 0.5, 0.5, 0.5);
        } else {
            ((ServerWorld) minecart.getWorld()).spawnParticles(ParticleTypes.ANGRY_VILLAGER, minecart.getX(), minecart.getY() + 0.2, minecart.getZ(), 10, 0.5, 0.5, 0.5, 0.5);
        }
        cir.setReturnValue(result);
    }
}