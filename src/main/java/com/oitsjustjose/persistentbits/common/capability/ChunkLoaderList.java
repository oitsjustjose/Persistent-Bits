package com.oitsjustjose.persistentbits.common.capability;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.oitsjustjose.persistentbits.PersistentBits;
import com.oitsjustjose.persistentbits.common.utils.CommonConfig;

import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
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
        if (this.contains(pos))
        {
            int nowInChunk = loadersPerChunk.get(pos.toLong()) + 1;
            loadersPerChunk.replace(pos.toLong(), nowInChunk);
        }
        else
        {
            loadersPerChunk.put(pos.toLong(), 1);
            if (!currentlyLoading)
            {
                forceLoad(pos);
            }
        }
        PersistentBits.getInstance().LOGGER.info("add: {}", loadersPerChunk);
    }

    @Override
    public void remove(BlockPos pos)
    {
        if (this.contains(pos))
        {
            int nowInChunk = loadersPerChunk.get(pos.toLong()) - 1;
            if (nowInChunk <= 0)
            {
                loadersPerChunk.remove(pos.toLong());
                if (!currentlyLoading)
                {
                    forceUnload(pos);
                }
            }
            else
            {
                loadersPerChunk.replace(pos.toLong(), nowInChunk);
            }
        }
        PersistentBits.getInstance().LOGGER.info("remove: {}", loadersPerChunk);
    }

    @Override
    public boolean contains(BlockPos pos)
    {
        return this.loadersPerChunk.containsKey(pos.toLong());
    }

    public void forceLoad(BlockPos pos)
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

                this.world.getServer().getCommandManager().handleCommand(source,
                        "forceload add " + (x << 4) + " " + (z << 4));
                if (CommonConfig.ENABLE_LOGGING.get())
                {
                    PersistentBits.getInstance().LOGGER.info("Now loading chunk [{}, {}]", x << 4, z << 4);
                }
            }
        }
    }

    public void forceUnload(BlockPos pos)
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
                this.world.getServer().getCommandManager().handleCommand(source,
                        "forceload remove " + (x << 4) + " " + (z << 4));
                if (CommonConfig.ENABLE_LOGGING.get())
                {
                    PersistentBits.getInstance().LOGGER.info("No longer loading chunk [{}, {}]", x << 4, z << 4);
                }
            }
        }
    }

}