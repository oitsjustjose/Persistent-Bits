package com.oitsjustjose.persistent_bits.security;

import com.mojang.authlib.GameProfile;
import com.oitsjustjose.persistent_bits.tileentity.TileChunkLoader;

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
		TileChunkLoader tile = (TileChunkLoader) world.getTileEntity(event.getPos());

		if (tile != null && tile.getOwner() != null)
		{
			GameProfile owner = tile.getOwner();
			if (!(owner.getId().equals(harvester.getId())))
			{
				event.setNewSpeed(0);
			}
		}
	}
}