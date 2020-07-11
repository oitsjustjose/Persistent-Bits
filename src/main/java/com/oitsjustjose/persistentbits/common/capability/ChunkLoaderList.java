/**
 * 99% of the credit goes to Lex Manos
 * https://bit.ly/2QHmbMI
 */

package com.oitsjustjose.persistentbits.common.capability;

import javax.annotation.Nullable;

import com.oitsjustjose.persistentbits.PersistentBits;
import com.oitsjustjose.persistentbits.common.utils.CommonConfig;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class ChunkLoaderList implements IChunkLoaderList {
    private final Long2IntMap refCount = new Long2IntOpenHashMap();
    private final LongSet loaders = new LongOpenHashSet();
    private boolean loading = false;
    @Nullable
    private final ServerWorld world;

    public ChunkLoaderList(@Nullable ServerWorld world) {
        refCount.defaultReturnValue(Integer.MIN_VALUE);
        this.world = world;
    }

    @Override
    public void add(BlockPos pos) {
        long block = pos.toLong();
        if (!loaders.contains(block)) {
            long chunk = toChunk(pos);
            int ref = refCount.get(chunk);
            if (ref == Integer.MIN_VALUE) {
                if (!loading) {
                    this.load(pos);
                }
                ref = 1;
            } else {
                ref += 1;
            }
            refCount.put(chunk, ref);
            loaders.add(block);
        }
    }

    @Override
    public void remove(BlockPos pos) {
        if (loaders.remove(pos.toLong())) {
            long chunk = toChunk(pos);
            int ref = refCount.get(chunk);

            if (ref == Integer.MIN_VALUE || --ref <= 0) {
                refCount.remove(chunk);
                if (!loading) {
                    if (this.world != null) {
                        this.unload(pos);
                    }
                }
            } else {
                refCount.put(chunk, ref);
            }
        }
    }

    @Override
    public void load(BlockPos pos) {
        if (this.world == null) {
            return;
        }

        ChunkPos tmp = new ChunkPos(pos);
        int radius = CommonConfig.LOADING_RADIUS.get();

        for (int x = tmp.x - radius; x <= tmp.x + radius; x++) {
            for (int z = tmp.z - radius; z <= tmp.z + radius; z++) {
                this.world.forceChunk(x, z, true);
            }
        }
    }

    @Override
    public void unload(BlockPos pos) {
        if (this.world == null) {
            return;
        }

        this.world.getCapability(PersistentBits.MINI_CAPABILITY, null).ifPresent((cap) -> {
            ChunkPos tmp = new ChunkPos(pos);
            int radius = CommonConfig.LOADING_RADIUS.get();

            for (int x = tmp.x - radius; x <= tmp.x + radius; x++) {
                for (int z = tmp.z - radius; z <= tmp.z + radius; z++) {
                    if (!cap.containsChunk(new ChunkPos(x, z))) {
                        if (!this.containsChunk(new ChunkPos(x, z))) {
                            this.world.forceChunk(x, z, false);
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean containsChunk(ChunkPos pos) {
        for (Long key : this.refCount.keySet()) {
            ChunkPos tmp = new ChunkPos(key);
            int radius = CommonConfig.LOADING_RADIUS.get();

            for (int x = tmp.x - radius; x <= tmp.x + radius; x++) {
                for (int z = tmp.z - radius; z <= tmp.z + radius; z++) {
                    if (x == pos.x && z == pos.z) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private long toChunk(BlockPos pos) {
        return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static class Storage implements IStorage<IChunkLoaderList> {
        @Override
        public INBT writeNBT(Capability<IChunkLoaderList> capability, IChunkLoaderList instance, Direction side) {
            if (!(instance instanceof ChunkLoaderList)) {
                return null;
            }
            ChunkLoaderList list = (ChunkLoaderList) instance;
            long[] data = new long[list.loaders.size()];
            int idx = 0;
            for (long l : list.loaders) {
                data[idx++] = l;
            }
            return new LongArrayNBT(data);
        }

        @Override
        public void readNBT(Capability<IChunkLoaderList> capability, IChunkLoaderList instance, Direction side,
                            INBT nbt) {
            if (!(instance instanceof ChunkLoaderList) || !(nbt instanceof LongArrayNBT)) {
                return;
            }
            ChunkLoaderList list = (ChunkLoaderList) instance;
            list.loading = true;
            list.refCount.clear();
            list.loaders.clear();
            try {
                for (long l : ((LongArrayNBT) nbt).getAsLongArray()) {
                    list.add(BlockPos.fromLong(l));
                }
                if (list.world != null) {
                    // Run the force commands next tick to make sure they wern't removed.
                    list.world.getServer().enqueue(new TickDelayedTask(1, () -> {
                        for (long l : list.refCount.keySet()) {
                            ChunkPos chunk = new ChunkPos(l);
                            list.load(new BlockPos(chunk.x << 4, 0, chunk.z << 4));
                        }
                    }));
                }
            } finally {
                list.loading = false;
            }
        }
    }
}