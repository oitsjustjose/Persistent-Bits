package com.oitsjustjose.persistentbits.common.capability;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.oitsjustjose.persistentbits.PersistentBits;
import com.oitsjustjose.persistentbits.common.utils.CommonConfig;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;

public class ChunkLoaderList implements IChunkLoaderList
{

    public HashMap<Long, Integer> loadersPerChunk = new HashMap<>();
    public boolean currentlyLoading = false;
    @Nullable
    public final ServerWorld world;

    public ChunkLoaderList(@Nullable ServerWorld world)
    {
        this.world = world;
    }

    @Override
    public void add(BlockPos pos)
    {
        ChunkPos asChunkPos = new ChunkPos(pos);
        if (this.contains(pos))
        {
            int nowInChunk = loadersPerChunk.get(asChunkPos.asLong()) + 1;
            loadersPerChunk.replace(asChunkPos.asLong(), nowInChunk);
        }
        else
        {
            loadersPerChunk.put(asChunkPos.asLong(), 1);
            if (!currentlyLoading)
            {
                forceLoad(pos);
            }
        }
        PersistentBits.getInstance().LOGGER.info(loadersPerChunk);
    }

    @Override
    public void remove(BlockPos pos)
    {
        ChunkPos asChunkPos = new ChunkPos(pos);
        if (this.contains(pos))
        {
            int nowInChunk = loadersPerChunk.get(asChunkPos.asLong()) - 1;
            if (nowInChunk <= 0)
            {
                loadersPerChunk.remove(asChunkPos.asLong());
                if (!currentlyLoading)
                {
                    forceUnload(pos);
                }
            }
            else
            {
                loadersPerChunk.replace(asChunkPos.asLong(), nowInChunk);
            }
        }
    }

    @Override
    public boolean contains(BlockPos pos)
    {
        ChunkPos asChunkPos = new ChunkPos(pos);
        return this.loadersPerChunk.containsKey(asChunkPos.asLong());
    }

    public void forceLoad(BlockPos pos)
    {

        if (this.world == null || this.world.getServer() == null)
        {
            return;
        }

        int radius = CommonConfig.LOADING_RADIUS.get();
        ChunkPos tmp = new ChunkPos(pos);

        for (int x = tmp.x - radius; x < tmp.x + radius; x++)
        {
            for (int z = tmp.z - radius; z < tmp.z + radius; z++)
            {
                this.world.forceChunk(x, z, true);
            }
        }
    }

    public void forceUnload(BlockPos pos)
    {

        if (this.world == null || this.world.getServer() == null)
        {
            return;
        }

        int radius = CommonConfig.LOADING_RADIUS.get();
        ChunkPos tmp = new ChunkPos(pos);

        for (int x = tmp.x - radius; x < tmp.x + radius; x++)
        {
            for (int z = tmp.z - radius; z < tmp.z + radius; z++)
            {
                this.world.forceChunk(x, z, false);
            }
        }
    }

}