package com.oitsjustjose.persistentbits.common.capability;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.oitsjustjose.persistentbits.PersistentBits;
import com.oitsjustjose.persistentbits.common.utils.ChunkPosDim;
import com.oitsjustjose.persistentbits.common.utils.CommonConfig;

import net.minecraft.command.CommandSource;
import net.minecraft.world.server.ServerWorld;

public class ChunkLoaderList implements IChunkLoaderList
{

    public HashMap<ChunkPosDim, Integer> loadersPerChunk = new HashMap<>();
    public boolean currentlyLoading = false;
    @Nullable
    public final ServerWorld world;

    public ChunkLoaderList(@Nullable ServerWorld world)
    {
        this.world = world;
    }

    @Override
    public void add(ChunkPosDim pos)
    {
        if (loadersPerChunk.containsKey(pos))
        {
            int nowInChunk = loadersPerChunk.get(pos) + 1;
            loadersPerChunk.replace(pos, nowInChunk);
        }
        else
        {
            loadersPerChunk.put(pos, 1);
            if (!currentlyLoading)
            {
                forceLoad(pos);
            }
        }
    }

    @Override
    public void remove(ChunkPosDim pos)
    {
        if (loadersPerChunk.containsKey(pos))
        {
            int nowInChunk = loadersPerChunk.get(pos) - 1;
            if (nowInChunk == 0)
            {
                loadersPerChunk.remove(pos);
                if (!currentlyLoading)
                {
                    forceUnload(pos);
                }
            }
            else
            {
                loadersPerChunk.replace(pos, nowInChunk);
            }
        }
    }

    @Override
    public boolean contains(ChunkPosDim pos)
    {
        return loadersPerChunk.keySet().contains(pos);
    }

    public void forceLoad(ChunkPosDim pos)
    {
        int radius = CommonConfig.LOADING_RADIUS.get();

        if (this.world == null || this.world.getServer() == null)
        {
            return;
        }

        CommandSource source = this.world.getServer().getCommandSource().withWorld(this.world);

        for (int x = pos.getX() - radius; x <= pos.getX() + radius; x++)
        {
            for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; z++)
            {
                this.world.getServer().getCommandManager().handleCommand(source, "forceload add " + x + " " + z);
                if (CommonConfig.ENABLE_LOGGING.get())
                {
                    PersistentBits.getInstance().LOGGER.info("Now loading chunk [{}, {}]", x, z);
                }
            }
        }
    }

    public void forceUnload(ChunkPosDim pos)
    {
        int radius = CommonConfig.LOADING_RADIUS.get();

        if (this.world == null || this.world.getServer() == null)
        {
            return;
        }

        CommandSource source = this.world.getServer().getCommandSource().withWorld(this.world);

        for (int x = pos.getX() - radius; x <= pos.getX() + radius; x++)
        {
            for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; z++)
            {
                this.world.getServer().getCommandManager().handleCommand(source, "forceload remove " + x + " " + z);
                if (CommonConfig.ENABLE_LOGGING.get())
                {
                    PersistentBits.getInstance().LOGGER.info("No longer loading chunk [{}, {}]", x, z);
                }
            }
        }
    }

}