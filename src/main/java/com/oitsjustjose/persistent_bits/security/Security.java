package com.oitsjustjose.persistent_bits.security;

/**
 * @author oitsjustjose
 * 
 * A simple class for allowing block breaking security on Chunk Loaders
 */

import com.mojang.authlib.GameProfile;
import com.oitsjustjose.persistent_bits.tileentity.TileChunkLoader;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Security
{
	@SubscribeEvent
	public void registerEvent(BreakSpeed event)
	{
		World world = event.getEntity().getEntityWorld();
		GameProfile harvester = new GameProfile(event.getEntityPlayer().getUniqueID(), event.getEntityPlayer().getName());
		TileEntity tile = world.getTileEntity(event.getPos());

		if (tile != null && tile instanceof TileChunkLoader)
		{
			TileChunkLoader tileChunkLoader = (TileChunkLoader) world.getTileEntity(event.getPos());

			if (tileChunkLoader.getOwner() != null)
			{
				GameProfile owner = tileChunkLoader.getOwner();
				if (!(owner.getId().equals(harvester.getId())))
				{
					event.setNewSpeed(0);
				}
			}
		}
	}
}