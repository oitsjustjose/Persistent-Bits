package com.oitsjustjose.persistent_bits.tileentity;

import java.util.LinkedList;
import java.util.List;

import com.oitsjustjose.persistent_bits.PersistentBits;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.ForgeChunkManager;

public class TileChunkLoader extends TileEntity
{
	private ForgeChunkManager.Ticket chunkTicket;

	public List<ChunkPos> getLoadArea()
	{
		List<ChunkPos> loadArea = new LinkedList<ChunkPos>();

		for (int xMod = -3; xMod < 4; xMod++)
		{
			for (int zMod = -3; zMod < 4; zMod++)
			{
				int chunkXNew = (this.getPos().getX() + (xMod * 16)) >> 4;
				int chunkZNew = (this.getPos().getZ() + (zMod * 16)) >> 4;

				loadArea.add(new ChunkPos(chunkXNew, chunkZNew));
			}
		}

		return loadArea;
	}

	@Override
	public void validate()
	{
		super.validate();
		if ((!this.worldObj.isRemote) && (this.chunkTicket == null))
		{
			ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(PersistentBits.instance, this.worldObj, ForgeChunkManager.Type.NORMAL);
			if (ticket != null)
			{
				forceChunkLoading(ticket);
			}
		}
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		stopChunkLoading();
	}

	public void forceChunkLoading(ForgeChunkManager.Ticket ticket)
	{
		stopChunkLoading();
		this.chunkTicket = ticket;
		for (ChunkPos coord : getLoadArea())
		{
			ForgeChunkManager.forceChunk(this.chunkTicket, coord);
		}
	}

	public void unforceChunkLoading()
	{
		for (Object obj : this.chunkTicket.getChunkList())
		{
			ChunkPos coord = (ChunkPos) obj;
			ForgeChunkManager.unforceChunk(this.chunkTicket, coord);
		}
	}

	public void stopChunkLoading()
	{
		if (this.chunkTicket != null)
		{
			ForgeChunkManager.releaseTicket(this.chunkTicket);
			this.chunkTicket = null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
	}
}