package com.github.vini2003.linkart.mixin;

import com.github.vini2003.linkart.api.LinkableMinecart;
import com.github.vini2003.linkart.utility.CollisionUtils;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
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

    @Shadow @Nullable public abstract ItemEntity dropStack(ItemStack stack);

    @Inject(at = @At("HEAD"), method = "remove")
    void linkart$removeLink(CallbackInfo callbackInformation) {
        if ((Entity) (Object) this instanceof AbstractMinecartEntity && !world.isClient()) {
            LinkableMinecart accessor = (LinkableMinecart) this;
            LinkableMinecart follower = (LinkableMinecart) accessor.linkart$getFollower();
            LinkableMinecart following = (LinkableMinecart) accessor.linkart$getFollowing();

            if (follower != null) {
                follower.linkart$setFollowing(null);
            }

            if (following != null) {
                following.linkart$setFollower(null);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", cancellable = true)
    void linkart$onRecalculateVelocity(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
        List<Entity> collisions = this.world.getOtherEntities((Entity) (Object) this, getBoundingBox().stretch(movement));

        if ((Entity) (Object) this instanceof AbstractMinecartEntity minecart) {
            for (Entity entity : collisions) {
                if (!CollisionUtils.shouldCollide((Entity) (Object) this, entity) && world.getBlockState(minecart.getBlockPos()).getBlock() instanceof AbstractRailBlock) {
                    cir.setReturnValue(movement);
                    cir.cancel();
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "kill")
    private void linkart$kill(CallbackInfo ci) {
        if ((Object) this instanceof LinkableMinecart minecart) {
            this.dropStack(minecart.linkart$getLinkItem());
        }
    }
}
