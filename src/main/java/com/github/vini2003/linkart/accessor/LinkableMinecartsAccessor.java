package com.github.vini2003.linkart.accessor;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;

public interface LinkableMinecartsAccessor {
    AbstractMinecartEntity linkart$getFollowing();

    void linkart$setFollowing(AbstractMinecartEntity following);

    AbstractMinecartEntity linkart$getFollower();

    void linkart$setFollower(AbstractMinecartEntity follower);

    ItemStack linkart$getLinkItem();

    void linkart$setLinkItem(ItemStack linkItem);
}
