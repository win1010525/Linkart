package com.github.vini2003.linkart.mixin;

import com.github.vini2003.linkart.utility.CartUtils;
import com.github.vini2003.linkart.utility.CollisionUtils;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public World world;

    @Shadow
    public abstract Box getBoundingBox();

    @Inject(at = @At("HEAD"), method = "remove")
    void linkart$removeLink(CallbackInfo callbackInformation, @Local Entity.RemovalReason reason) {
        if ((Entity) (Object) this instanceof AbstractMinecartEntity minecart && !world.isClient() && reason.shouldDestroy()) {
            if (minecart.linkart$getFollowing() != null) CartUtils.unlink(minecart);
            if (minecart.linkart$getFollower() != null) CartUtils.unlink(minecart.linkart$getFollower());
        }
    }

    @Inject(at = @At("HEAD"), method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", cancellable = true)
    void linkart$onRecalculateVelocity(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
        if ((Object) this instanceof AbstractMinecartEntity minecart) {
            List<Entity> collisions = this.world.getOtherEntities((Entity) (Object) this, getBoundingBox().stretch(movement));

            for (Entity entity : collisions) {
                if (!CollisionUtils.shouldCollide((Entity) (Object) this, entity) && world.getBlockState(minecart.getBlockPos()).getBlock() instanceof AbstractRailBlock) {
                    cir.setReturnValue(movement);
                    cir.cancel();
                }
            }
        }
    }
}
