package com.oitsjustjose.persistent_bits.chunkloading;

import java.util.List;

import com.oitsjustjose.persistent_bits.tileentity.TileChunkLoader;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class ChunkLoadingCallback implements LoadingCallback
{
	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world)
	{
		for (Ticket ticket : tickets) 
		{
			int x = ticket.getModData().getInteger("xCoord");
			int y = ticket.getModData().getInteger("yCoord");
			int z = ticket.getModData().getInteger("zCoord");
			TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
			if ((tile instanceof TileChunkLoader)) 
			{
				((TileChunkLoader) tile).forceChunkLoading(ticket);
			}
		}
	}
}
