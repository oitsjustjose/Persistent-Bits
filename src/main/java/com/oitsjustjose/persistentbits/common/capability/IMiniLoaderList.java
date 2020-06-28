
/**
 * All credit to LexManos (although this is trivial):
 * https://bit.ly/2LZV1P7
 */

package com.oitsjustjose.persistentbits.common.capability;

import net.minecraft.util.math.BlockPos;

public interface IMiniLoaderList {
    void add(BlockPos pos);

    void remove(BlockPos pos);

    void load(BlockPos pos);

    void unload(BlockPos pos);

    boolean chunkContains(BlockPos pos);

    boolean contains(BlockPos pos);
}