package com.oitsjustjose.persistentbits.common.capability;

import javax.annotation.Nullable;

import com.oitsjustjose.persistentbits.PersistentBits;

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

public class MiniLoaderList implements IMiniLoaderList {

    private final Long2IntMap refCount = new Long2IntOpenHashMap();
    private final LongSet loaders = new LongOpenHashSet();
    private boolean loading = false;
    @Nullable
    private final ServerWorld world;

    public MiniLoaderList(@Nullable ServerWorld world) {
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
                if (!loading) {
                    if(this.world != null){
                        this.world.getCapability(PersistentBits.CAPABILITY, null).ifPresent((cap) -> {
                            ChunkPos tmp = new ChunkPos(pos);
                            if (!cap.containsChunk(tmp)) {
                                this.unload(pos);
                            }
                        });
                    }
                }
                refCount.remove(chunk);
            } else {
                refCount.put(chunk, ref);
            }
        }

    }

    @Override
    public void load(BlockPos pos) {
        ChunkPos tmp = new ChunkPos(pos);
        if(this.world != null) {
            this.world.forceChunk(tmp.x, tmp.z, true);
        }
    }

    @Override
    public void unload(BlockPos pos) {
        ChunkPos tmp = new ChunkPos(pos);
        if(this.world != null){
            this.world.forceChunk(tmp.x, tmp.z, false);
        }
    }

    @Override
    public boolean containsChunk(ChunkPos pos) {
        int ref = refCount.get(pos.asLong());
        return !(ref <= 0);
    }

    private long toChunk(BlockPos pos) {
        return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static class Storage implements IStorage<IMiniLoaderList> {
        @Override
        public INBT writeNBT(Capability<IMiniLoaderList> capability, IMiniLoaderList instance, Direction side) {
            if (!(instance instanceof MiniLoaderList)) {
                return null;
            }
            MiniLoaderList list = (MiniLoaderList) instance;
            long[] data = new long[list.loaders.size()];
            int idx = 0;
            for (long l : list.loaders) {
                data[idx++] = l;
            }
            return new LongArrayNBT(data);
        }

        @Override
        public void readNBT(Capability<IMiniLoaderList> capability, IMiniLoaderList instance, Direction side,
                INBT nbt) {
            if (!(instance instanceof MiniLoaderList) || !(nbt instanceof LongArrayNBT)) {
                return;
            }
            MiniLoaderList list = (MiniLoaderList) instance;
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
                            ChunkPos tmp = new ChunkPos(l);
                            list.world.forceChunk(tmp.x, tmp.z, true);
                        }
                    }));
                }
            } finally {
                list.loading = false;
            }
        }
    }
}