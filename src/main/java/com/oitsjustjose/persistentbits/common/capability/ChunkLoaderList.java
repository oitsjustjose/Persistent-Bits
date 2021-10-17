/**
 * 99% of the credit goes to Lex Manos
 * https://bit.ly/2QHmbMI
 */

package com.oitsjustjose.persistentbits.common.capability;

import com.oitsjustjose.persistentbits.common.utils.CommonConfig;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import javax.annotation.Nullable;

public class ChunkLoaderList implements IChunkLoaderList {
    private final Long2IntMap refCount = new Long2IntOpenHashMap();
    private final LongSet loaders = new LongOpenHashSet();
    private final ServerLevel world;
    private boolean loading = false;

    public static final Capability<IChunkLoaderList> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public ChunkLoaderList(@Nullable ServerLevel world) {
        refCount.defaultReturnValue(Integer.MIN_VALUE);
        this.world = world;
    }

    @Override
    public void add(BlockPos pos) {
        long block = pos.asLong();
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
        if (loaders.remove(pos.asLong())) {
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
                this.world.setChunkForced(x, z, true);
            }
        }
    }

    @Override
    public void unload(BlockPos pos) {
        if (this.world == null) {
            return;
        }

        this.world.getCapability(CAPABILITY, null).ifPresent((cap) -> {
            ChunkPos tmp = new ChunkPos(pos);
            int radius = CommonConfig.LOADING_RADIUS.get();

            for (int x = tmp.x - radius; x <= tmp.x + radius; x++) {
                for (int z = tmp.z - radius; z <= tmp.z + radius; z++) {
                    if (!cap.containsChunk(new ChunkPos(x, z))) {
                        if (!this.containsChunk(new ChunkPos(x, z))) {
                            this.world.setChunkForced(x, z, false);
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

    public CompoundTag serializeNBT() {
        CompoundTag ret = new CompoundTag();

        // Create LongArrayNBT from current dataset
        long[] data = new long[this.loaders.size()];
        int idx = 0;
        for (long l : this.loaders) {
            data[idx++] = l;
        }
        LongArrayTag asLat = new LongArrayTag(data);
        ret.put("data", asLat);
        return ret;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.loading = true;
        this.refCount.clear();
        this.loaders.clear();

        try {
            for (long l : nbt.getLongArray("data")) {
                this.add(BlockPos.of(l));
            }

            if (this.world != null) {
                this.world.getServer().addTickable(new TickTask(1, () -> {
                    for (long l : this.refCount.keySet()) {
                        ChunkPos cp = new ChunkPos(l);
                        this.load(cp.getWorldPosition());
                    }
                }));
            }

        } finally {
            this.loading = false;
        }
    }
}