package com.github.vini2003.linkart.mixin;

import com.github.vini2003.linkart.Linkart;
import com.github.vini2003.linkart.accessor.LinkableMinecartsAccessor;
import com.github.vini2003.linkart.utility.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.github.vini2003.linkart.utility.TextUtil.blockPosAsString;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    private static void linkart$spawnChainParticles(AbstractMinecartEntity entity, LinkableMinecartsAccessor duck) {
        if (!entity.world.isClient()) {
            ((ServerWorld) entity.world).spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, duck.linkart$getLinkItem()), entity.getX(), entity.getY() + 0.3, entity.getZ(), 15, 0.2, 0.2, 0.2, 0.2);
        }
    }

    @Inject(at = @At("HEAD"), method = "interact", cancellable = true)
    void onInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (entity instanceof AbstractMinecartEntity) {
            AbstractMinecartEntity minecart = (AbstractMinecartEntity) entity;
            if (!world.isClient()) {
                PlayerEntity player = (PlayerEntity) (Object) this;
                LinkableMinecartsAccessor duck = (LinkableMinecartsAccessor) minecart;
                ItemStack stack = player.getStackInHand(hand);

                if (stack.getItem().isIn(Linkart.LINKERS)) {
                    if (Linkart.UNLINKING_CARTS.containsKey(player)) {
                        AbstractMinecartEntity unlinking = Linkart.UNLINKING_CARTS.get(player);
                        if (unlinking == null) {
                            linkart$failLink(player, TextUtil.translatable("linkart.de_sync"));
                            cir.setReturnValue(ActionResult.FAIL);
                        } else if (unlinking == minecart) {
                            linkart$failLink(player, TextUtil.translatable("linkart.link_self"));
                            cir.setReturnValue(ActionResult.FAIL);
                        } else {
                            LinkableMinecartsAccessor duck1 = (LinkableMinecartsAccessor) unlinking;
                            if (duck1.linkart$getFollower() == minecart) {

                                duck.linkart$setFollowing(null);
                                duck1.linkart$setFollower(null);

                                ItemEntity itemEntity = new ItemEntity(world, minecart.getX(), minecart.getY(), minecart.getZ(), duck.linkart$getLinkItem());
                                itemEntity.setToDefaultPickupDelay();
                                world.spawnEntity(itemEntity);

                                linkart$spawnChainParticles(minecart, duck);

                                duck.linkart$setLinkItem(null);

                                linkart$linkSuccess(player, TextUtil.translatable("linkart.finished_unlink",
                                        minecart.hasCustomName() ? minecart.getCustomName() : blockPosAsString(minecart.getBlockPos()),
                                        unlinking.hasCustomName() ? unlinking.getCustomName() : blockPosAsString(unlinking.getBlockPos())));
                                cir.setReturnValue(ActionResult.SUCCESS);
                            } else {
                                linkart$failLink(player, TextUtil.translatable("linkart.what"));
                                cir.setReturnValue(ActionResult.FAIL);
                            }
                        }
                        Linkart.UNLINKING_CARTS.remove(player);
                    } else if (Linkart.LINKING_CARTS.containsKey(player)) {
                        AbstractMinecartEntity linkingTo = Linkart.LINKING_CARTS.get(player);

                        if (linkingTo == null) {
                            linkart$failLink(player, TextUtil.translatable("linkart.de_sync"));
                            cir.setReturnValue(ActionResult.FAIL);
                        } else if (linkingTo == minecart) {
                            linkart$failLink(player, TextUtil.translatable("linkart.link_self"));
                            cir.setReturnValue(ActionResult.FAIL);
                        } else if (Math.abs(minecart.distanceTo(linkingTo) - 1) > Linkart.CONFIG.pathfindingDistance) {
                            linkart$failLink(player, TextUtil.translatable("linkart.too_far"));
                            cir.setReturnValue(ActionResult.FAIL);
                        } else {
                            LinkableMinecartsAccessor duck1 = (LinkableMinecartsAccessor) linkingTo;

                            duck.linkart$setFollowing(linkingTo);
                            duck1.linkart$setFollower(minecart);

                            if (!player.isCreative()) stack.decrement(1);

                            ItemStack linkStack = stack.copy();
                            linkStack.setCount(1);
                            duck.linkart$setLinkItem(linkStack);

                            linkart$spawnChainParticles(minecart, duck);

                            linkart$linkSuccess(player, TextUtil.translatable("linkart.finished_link",
                                    minecart.hasCustomName() ? minecart.getCustomName() : blockPosAsString(minecart.getBlockPos()),
                                    linkingTo.hasCustomName() ? linkingTo.getCustomName() : blockPosAsString(linkingTo.getBlockPos())));
                            cir.setReturnValue(ActionResult.SUCCESS);
                        }
                        Linkart.LINKING_CARTS.remove(player);
                    } else if (duck.linkart$getFollower() != null) {
                        Linkart.UNLINKING_CARTS.put(player, minecart);
                        ((ServerWorld) entity.world).spawnParticles(ParticleTypes.HAPPY_VILLAGER, minecart.getX(), minecart.getY() + 0.2, minecart.getZ(), 10, 0.5, 0.5, 0.5, 0.5);
                        linkart$linkSuccess(player, TextUtil.translatable("linkart.start_unlink",
                                minecart.hasCustomName() ? minecart.getCustomName() : blockPosAsString(minecart.getBlockPos())));
                        cir.setReturnValue(ActionResult.SUCCESS);
                    } else {
                        Linkart.LINKING_CARTS.put(player, minecart);
                        ((ServerWorld) entity.world).spawnParticles(ParticleTypes.HAPPY_VILLAGER, minecart.getX(), minecart.getY() + 0.2, minecart.getZ(), 10, 0.5, 0.5, 0.5, 0.5);
                        linkart$linkSuccess(player, TextUtil.translatable("linkart.start_link",
                                minecart.hasCustomName() ? minecart.getCustomName() : blockPosAsString(minecart.getBlockPos())));
                        cir.setReturnValue(ActionResult.SUCCESS);
                    }
                }
            }
        }
    }

    private void linkart$failLink(PlayerEntity player, MutableText msg) {
        player.sendMessage(msg.formatted(Formatting.RED), true);
    }

    private void linkart$linkSuccess(PlayerEntity player, MutableText msg) {
        player.sendMessage(msg.formatted(Formatting.GREEN), true);
    }
}