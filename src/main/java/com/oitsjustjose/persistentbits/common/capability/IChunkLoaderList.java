
/**
 * All credit to LexManos (although this is trivial):
 * https://bit.ly/2LZV1P7
 */

package com.oitsjustjose.persistentbits.common.capability;

import com.oitsjustjose.persistentbits.common.utils.ChunkPosDim;

public interface IChunkLoaderList
{
    void add(ChunkPosDim pos);

    void remove(ChunkPosDim pos);

    boolean contains(ChunkPosDim pos);
}