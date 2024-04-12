package com.github.vini2003.linkart.utility;

import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.base.util.Mapper;
import me.melontini.dark_matter.api.base.util.Utilities;
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
import net.minecraft.world.PersistentStateManager;

import java.lang.invoke.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class LoadingCarts extends PersistentState {

    private static final Function<PersistentStateManager, LoadingCarts> GETTER = Exceptions.supply(() -> {
        Supplier<LoadingCarts> supplier = LoadingCarts::new;
        Function<NbtCompound, LoadingCarts> function = nbt -> new LoadingCarts().readNbt(nbt);
        try {
            Type<LoadingCarts> type = new Type<>(supplier, function, null);//thanks, FAPI
            return manager -> manager.getOrCreate(type, "linkart_loading_carts");
        } catch (Throwable e) {
            String mth = Mapper.mapMethod(PersistentStateManager.class, "method_17924", MethodType.methodType(PersistentState.class, Function.class, Supplier.class, String.class));
            MethodHandle h = Exceptions.supply(() -> MethodHandles.lookup().findVirtual(PersistentStateManager.class, mth, MethodType.methodType(PersistentState.class, Function.class, Supplier.class, String.class)));

            //Not pretty, but is faster than reflection and handles.
            interface Invoker {
                PersistentState invoke(PersistentStateManager manager, Function<?,?> f, Supplier<?> s, String name);
            }

            Invoker invoker = Utilities.makeLambda(MethodHandles.lookup(), Invoker.class, h);
            return manager -> (LoadingCarts) invoker.invoke(manager, function, supplier, "linkart_loading_carts");
        }
    });

    public static LoadingCarts getOrCreate(ServerWorld world) {
        return GETTER.apply(world.getPersistentStateManager());
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
