package com.github.vini2003.linkart.mixin;

import com.github.vini2003.linkart.Linkart;
import com.github.vini2003.linkart.utility.CartUtils;
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

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "interact", cancellable = true)
    void onInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (entity instanceof AbstractMinecartEntity minecart) {
            if (!getWorld().isClient()) {
                PlayerEntity player = (PlayerEntity) (Object) this;
                ItemStack stack = player.getStackInHand(hand);

                if (stack.isIn(Linkart.LINKERS)) {
                    if (Linkart.UNLINKING_CARTS.containsKey(player)) {
                        AbstractMinecartEntity unlinking = Linkart.UNLINKING_CARTS.get(player);
                        if (unlinking == null || unlinking == minecart) {
                            fail(cir, minecart);
                        } else {
                            if (unlinking.linkart$getFollower() == minecart) {
                                CartUtils.unlink(minecart);
                                cir.setReturnValue(ActionResult.SUCCESS);
                            } else {
                                fail(cir, minecart);
                            }
                        }
                        Linkart.UNLINKING_CARTS.remove(player);
                    } else if (Linkart.LINKING_CARTS.containsKey(player)) {
                        AbstractMinecartEntity linkingTo = Linkart.LINKING_CARTS.get(player);

                        if (linkingTo == null || linkingTo == minecart ||
                                minecart.linkart$getFollower() == linkingTo ||
                                Math.abs(minecart.distanceTo(linkingTo) - 1) > Linkart.CONFIG.pathfindingDistance) {
                            fail(cir, minecart);
                        } else {
                            if (!player.isCreative()) stack.decrement(1);
                            CartUtils.link(minecart, linkingTo, stack);
                            cir.setReturnValue(ActionResult.SUCCESS);
                        }
                        Linkart.LINKING_CARTS.remove(player);
                    } else if (minecart.linkart$getFollower() != null) {
                        Linkart.UNLINKING_CARTS.put(player, minecart);
                        success(cir, minecart);
                    } else {
                        Linkart.LINKING_CARTS.put(player, minecart);
                        success(cir, minecart);
                    }
                }
            }
        }
    }

    @Unique
    private static void fail(CallbackInfoReturnable<ActionResult> cir, AbstractMinecartEntity minecart) {
        ((ServerWorld) minecart.getWorld()).spawnParticles(ParticleTypes.ANGRY_VILLAGER, minecart.getX(), minecart.getY() + 0.2, minecart.getZ(), 10, 0.5, 0.5, 0.5, 0.5);
        cir.setReturnValue(ActionResult.FAIL);
    }

    @Unique
    private static void success(CallbackInfoReturnable<ActionResult> cir, AbstractMinecartEntity minecart) {
        ((ServerWorld) minecart.getWorld()).spawnParticles(ParticleTypes.HAPPY_VILLAGER, minecart.getX(), minecart.getY() + 0.2, minecart.getZ(), 10, 0.5, 0.5, 0.5, 0.5);
        cir.setReturnValue(ActionResult.SUCCESS);
    }
}