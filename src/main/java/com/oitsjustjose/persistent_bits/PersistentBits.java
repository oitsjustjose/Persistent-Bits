package com.oitsjustjose.persistent_bits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.oitsjustjose.persistent_bits.blocks.BlockChunkLoader;
import com.oitsjustjose.persistent_bits.chunkloading.ChunkLoadingCallback;
import com.oitsjustjose.persistent_bits.chunkloading.ChunkLoadingDatabase;
import com.oitsjustjose.persistent_bits.chunkloading.DetailedCoordinate;
import com.oitsjustjose.persistent_bits.proxy.ClientProxy;
import com.oitsjustjose.persistent_bits.proxy.CommonProxy;
import com.oitsjustjose.persistent_bits.tileentity.TileChunkLoader;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = Lib.MODID, name = Lib.NAME, version = Lib.VERSION, acceptedMinecraftVersions = "1.9.4")
public class PersistentBits
{
	@Instance(Lib.MODID)
	public static PersistentBits instance;

	@SidedProxy(clientSide = Lib.CLIENT_PROXY, serverSide = Lib.COMMON_PROXY, modId = Lib.MODID)
	public static CommonProxy proxy;

	public static Logger LOGGER = LogManager.getLogger(Lib.MODID);
	public static Config config;
	public static Block chunkLoader;
	public static ChunkLoadingDatabase database;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		config = new Config(event.getSuggestedConfigurationFile());
		chunkLoader = new BlockChunkLoader();
		ForgeChunkManager.setForcedChunkLoadingCallback(instance, new ChunkLoadingCallback());
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(chunkLoader, 1), new Object[] { " E ", "DOD", "OXO", 'E', Items.ENDER_PEARL, 'D', "gemDiamond", 'O', Blocks.OBSIDIAN, 'X', Blocks.ENCHANTING_TABLE }));
	}

	@EventHandler
	public void postInit(FMLInitializationEvent event)
	{
		if (event.getSide().isClient())
		{
			ClientProxy.register(Item.getItemFromBlock(chunkLoader));
		}
	}

	@EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
		// Handles re-loading Chunk Loaders on world load
		database = new ChunkLoadingDatabase();

		WorldServer world;
		database.deserialize();
		for (DetailedCoordinate detCoord : database.getCoordinates())
		{
			world = DimensionManager.getWorld(detCoord.getDimensionID());
			if (world != null && !world.isRemote)
			{
				TileChunkLoader chunkLoader = (TileChunkLoader) world.getTileEntity(detCoord.getPos());
				if (chunkLoader != null)
				{
					world.loadedTileEntityList.add(chunkLoader);
					chunkLoader.setWorldObj(world);
					chunkLoader.validate();
					if (config.enableNotification)
						LOGGER.info("The Chunk Loader at " + detCoord + " has been automatically loaded!");
				}
			}
		}
	}
}
