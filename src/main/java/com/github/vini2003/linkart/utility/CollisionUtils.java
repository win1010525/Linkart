package com.github.vini2003.linkart.utility;

import com.github.vini2003.linkart.Linkart;
import com.github.vini2003.linkart.accessor.LinkableMinecartsAccessor;
import net.minecraft.entity.Entity;

public class CollisionUtils {
    public static boolean shouldCollide(Entity source, Entity target) {
        if (source instanceof LinkableMinecartsAccessor) {
            LinkableMinecartsAccessor check = (LinkableMinecartsAccessor) source;
            int i = 0;

            do {
                if (check == target) {
                    return false;
                }

                check = (LinkableMinecartsAccessor) check.linkart$getFollower();
                ++i;
            } while (check != null && i < Linkart.CONFIG.collisionDepth);

            check = (LinkableMinecartsAccessor) source;
            i = 0;

            while (check != target) {
                check = (LinkableMinecartsAccessor) check.linkart$getFollowing();
                ++i;
                if (check == null || i >= Linkart.CONFIG.collisionDepth) {
                    return true;
                }
            }

            return false;
        } else {
            return true;
        }
    }
}
