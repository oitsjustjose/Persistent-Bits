
/**
 * All credit to LexManos (although this is trivial):
 * https://bit.ly/2LZV1P7
 */

package com.oitsjustjose.persistentbits.common.capability;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public interface IMiniLoaderList {
    void add(BlockPos pos);

    void remove(BlockPos pos);

    void load(BlockPos pos);

    void unload(BlockPos pos);

    boolean containsChunk(ChunkPos pos);

    boolean contains(BlockPos pos);
}