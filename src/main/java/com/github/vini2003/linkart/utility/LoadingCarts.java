package com.github.vini2003.linkart.utility;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;

import java.util.HashSet;
import java.util.Set;

public class LoadingCarts extends PersistentState {

    public static LoadingCarts getOrCreate(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                (nbt) -> new LoadingCarts().readNbt(nbt),
                LoadingCarts::new, "linkart_loading_carts");
    }

    private final Set<BlockPos> chunksToReload = new HashSet<>();
    private final Set<AbstractMinecartEntity> cartsToBlockPos = new HashSet<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (AbstractMinecartEntity minecart : cartsToBlockPos) {
            if (!minecart.isRemoved()) list.add(NbtLong.of(minecart.getBlockPos().asLong()));
        }
        nbt.put("chunksToSave", list);
        cartsToBlockPos.clear();
        return nbt;
    }

    public LoadingCarts readNbt(NbtCompound nbt) {
        NbtList list = nbt.getList("chunksToSave", NbtElement.LONG_TYPE);
        for (NbtElement element : list) {
            chunksToReload.add(BlockPos.fromLong(((NbtLong) element).longValue()));
        }
        return this;
    }

    public void tick(ServerWorld world) {
        if (!chunksToReload.isEmpty()) {
            for (BlockPos pos : chunksToReload) {
                ChunkPos chunkPos = new ChunkPos(pos);
                world.getChunkManager().addTicket(ChunkTicketType.PORTAL, chunkPos, 4, pos);
            }
            chunksToReload.clear();
            markDirty();
        }
    }

    public void addCart(AbstractMinecartEntity cart) {
        cartsToBlockPos.add(cart);
        markDirty();
    }

    public void removeCart(AbstractMinecartEntity cart) {
        cartsToBlockPos.remove(cart);
        markDirty();
    }
}
