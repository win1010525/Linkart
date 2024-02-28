package com.github.vini2003.linkart.utility;

import com.github.vini2003.linkart.Linkart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

import java.util.Optional;

public record CartOperation(Operation operation, AbstractMinecartEntity minecart) {

    public enum Operation {
        LINKING {
            @Override
            public ActionResult perform(AbstractMinecartEntity minecart, CartOperation operation, Optional<PlayerEntity> player, ItemStack stack) {
                if (minecart.linkart$getFollower() == operation.minecart() ||
                        Math.abs(minecart.distanceTo(operation.minecart()) - 1) > Linkart.CONFIG.pathfindingDistance) {
                    return ActionResult.FAIL;
                }

                CartUtils.linkTo(minecart, operation.minecart(), stack);
                player.filter(player1 -> !player1.isCreative()).ifPresent(player1 -> stack.decrement(1));
                return ActionResult.SUCCESS;
            }
        },
        UNLINKING {
            @Override
            public ActionResult perform(AbstractMinecartEntity minecart, CartOperation operation, Optional<PlayerEntity> player, ItemStack stack) {
                if (operation.minecart().linkart$getFollower() == minecart) {
                    CartUtils.unlinkFromParent(minecart);
                    return ActionResult.SUCCESS;
                }
                return ActionResult.FAIL;
            }
        };

        public abstract ActionResult perform(AbstractMinecartEntity minecart, CartOperation operation, Optional<PlayerEntity> player, ItemStack stack);
    }
}
