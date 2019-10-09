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
        if (this.contains(pos))
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
        PersistentBits.getInstance().LOGGER.info("Received {} to remove", pos);
        if (this.contains(pos))
        {
            PersistentBits.getInstance().LOGGER.info("containsKey");
            int nowInChunk = loadersPerChunk.get(pos) - 1;
            PersistentBits.getInstance().LOGGER.info("nowInChunk");
            if (nowInChunk == 0)
            {
                loadersPerChunk.remove(pos);
                if (!currentlyLoading)
                {
                    PersistentBits.getInstance().LOGGER.info("should forceUnload()");
                    forceUnload(pos);
                }
            }
            else
            {
                PersistentBits.getInstance().LOGGER.info("won't forceUnload");
                loadersPerChunk.replace(pos, nowInChunk);
            }
        }
        else
        {
            PersistentBits.getInstance().LOGGER.info("loadersPerChunk not contain???");
            PersistentBits.getInstance().LOGGER.info("Contents of loadersPerChunk:\n{}", loadersPerChunk);

        }
    }

    @Override
    public boolean contains(ChunkPosDim pos)
    {
        for (ChunkPosDim chunkPosDim : loadersPerChunk.keySet())
        {
            if (chunkPosDim.equals(pos))
            {
                return true;
            }
        }
        return false;
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

                this.world.getServer().getCommandManager().handleCommand(source,
                        "forceload add " + (x << 4) + " " + (z << 4));
                if (CommonConfig.ENABLE_LOGGING.get())
                {
                    PersistentBits.getInstance().LOGGER.info("Now loading chunk [{}, {}]", x << 4, z << 4);
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