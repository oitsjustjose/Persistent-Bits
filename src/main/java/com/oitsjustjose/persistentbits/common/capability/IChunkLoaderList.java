
/**
 * All credit to LexManos (although this is trivial):
 * https://bit.ly/2LZV1P7
 */

package com.oitsjustjose.persistentbits.common.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public interface IChunkLoaderList {
    void add(BlockPos pos);

    void remove(BlockPos pos);

    void load(BlockPos pos);

    void unload(BlockPos pos);

    boolean containsChunk(ChunkPos pos);

    public CompoundTag serializeNBT();

    public void deserializeNBT(CompoundTag nbt);
}