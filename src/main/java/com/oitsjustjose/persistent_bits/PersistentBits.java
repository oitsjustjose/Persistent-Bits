package com.oitsjustjose.persistent_bits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.oitsjustjose.persistent_bits.blocks.BlockChunkLoader;
import com.oitsjustjose.persistent_bits.chunkloading.ChunkLoadingCallback;
import com.oitsjustjose.persistent_bits.proxy.ClientProxy;
import com.oitsjustjose.persistent_bits.tileentity.TileChunkLoader;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = Lib.MODID, name = Lib.NAME, version = Lib.VERSION, acceptedMinecraftVersions = "1.9.4")
public class PersistentBits
{
	@Instance(Lib.MODID)
	public static PersistentBits instance;
	
	public static Logger LOGGER = LogManager.getLogger(Lib.MODID);
	public static Block chunkLoader;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		chunkLoader = new BlockChunkLoader();
		GameRegistry.registerTileEntity(TileChunkLoader.class, Lib.MODID + "chunk_loader");
		ForgeChunkManager.setForcedChunkLoadingCallback(instance, new ChunkLoadingCallback());
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(chunkLoader, 1), new Object[] { " E ", "DOD", "OXO", 'E', Items.ENDER_PEARL, 'D', "gemDiamond", 'O', Blocks.OBSIDIAN, 'X', Blocks.ENCHANTING_TABLE }));
	}

	@EventHandler
	public void postInit(FMLInitializationEvent event)
	{
		if(event.getSide().isClient())
			ClientProxy.register(Item.getItemFromBlock(chunkLoader));
	}
}
