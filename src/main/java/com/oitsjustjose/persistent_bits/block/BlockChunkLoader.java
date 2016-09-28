package com.oitsjustjose.persistent_bits.block;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;
import com.oitsjustjose.persistent_bits.Lib;
import com.oitsjustjose.persistent_bits.PersistentBits;
import com.oitsjustjose.persistent_bits.chunkloading.DimCoordinate;
import com.oitsjustjose.persistent_bits.security.Security;
import com.oitsjustjose.persistent_bits.tileentity.TileChunkLoader;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockChunkLoader extends BlockContainer
{
	public ChatUtil chatUtil;

	public BlockChunkLoader()
	{
		super(Material.ROCK);
		this.initializeBlock();
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
	}

	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileChunkLoader();

	}

	// Toggles the loaded-chunk indicator
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		TileChunkLoader chunkTile = (TileChunkLoader) world.getTileEntity(pos);
		if (chunkTile != null)
			toggleVisualization(world, pos, player, chunkTile);

		player.swingArm(EnumHand.MAIN_HAND);
		return true;
	}

	// Sets the owner properly if security is enabled
	// Adds the DimCoordinate to the serialized database
	// Log notification of placement
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if (placer instanceof EntityPlayer && !world.isRemote)
		{
			EntityPlayer player = (EntityPlayer) placer;
			GameProfile ownerProfile = new GameProfile(player.getUniqueID(), player.getName());
			TileChunkLoader chunkTile = (TileChunkLoader) world.getTileEntity(pos);
			if (chunkTile != null)
				chunkTile.setOwner(ownerProfile);
			if (PersistentBits.config.enableNotification)
				PersistentBits.LOGGER.info("Player " + player.getName() + " has placed a Chunk Loader at coordinates: x = " + pos.getX() + ", y = " + pos.getY() + ", z = " + pos.getZ() + " in Dimension " + world.provider.getDimension() + ".");
			PersistentBits.database.addChunkCoord(new DimCoordinate(pos, world.provider.getDimension()));
		}

		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}

	// Removed the DimCoordinate from the serialized database
	// Log notification of removal
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		if (!world.isRemote)
		{
			if (PersistentBits.config.enableNotification)
				PersistentBits.LOGGER.info("Chunk Loader at coordinates: x = " + pos.getX() + ", y = " + pos.getY() + ", z = " + pos.getZ() + " in Dimension " + world.provider.getDimension() + " has been destroyed.");
			// In a try/catch to avoid the block from just disappearing if it for some reason can't re-serialize the .dat
			try
			{
				PersistentBits.database.removeChunkCoord(new DimCoordinate(pos, world.provider.getDimension()));
			}
			catch (ConcurrentModificationException e)
			{
				PersistentBits.LOGGER.info("Concurrent Modification Exception caught!");
				super.breakBlock(world, pos, state);
			}
		}
	}

	/**
	 * Toggles an in-world representation of which chunks are loaded
	 * 
	 * @param world
	 *            Block / TE's worldObj
	 * @param pos
	 *            Block / TE's Position
	 * @param player
	 *            Player who activated block
	 * @param chunkTile
	 *            TE that was right-clicked
	 */
	public void toggleVisualization(World world, BlockPos pos, @Nullable EntityPlayer player, TileChunkLoader chunkTile)
	{
		if (world.isRemote)
		{
			List<BlockPos> chunkCenters = new LinkedList<BlockPos>();
			List<ChunkPos> area = chunkTile.getLoadArea();
			// Chosen for simplification of changing the block (for debug)
			IBlockState marker = Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlassPane.COLOR, EnumDyeColor.RED);

			for (ChunkPos c : area)
				chunkCenters.add(c.getCenterBlock(pos.getY()));

			if (chunkTile.isShowingChunks())
			{
				chunkTile.setChunksHidden();
				if (player != null)
					this.chatUtil.sendNoSpamMessages(new TextComponentString("Loaded Chunks hidden").setStyle(new Style().setColor(TextFormatting.DARK_PURPLE)));
				for (BlockPos p : chunkCenters)
					for (int i = 0; p.up(i).getY() < p.getY() + PersistentBits.config.maxHeightIndicator; i++)
						if (world.getBlockState(p.up(i)) == marker)
							world.setBlockToAir(p.up(i));
			}
			else
			{
				chunkTile.setChunksShown();

				if (player != null)
					this.chatUtil.sendNoSpamMessages(new TextComponentString("Loaded Chunks shown").setStyle(new Style().setColor(TextFormatting.AQUA)));

				for (BlockPos p : chunkCenters)
					for (int i = 0; p.up(i).getY() < p.getY() + PersistentBits.config.maxHeightIndicator; i++)
						if (world.isAirBlock(p.up(i)))
							world.setBlockState(p.up(i), marker);
			}
		}
	}

	/**
	 * A condensed way of doing boring block init
	 */
	public void initializeBlock()
	{
		// Standard block constructor stuff
		this.setHardness(10F);
		this.setResistance(1000F);
		this.setSoundType(SoundType.STONE);
		this.setCreativeTab(CreativeTabs.REDSTONE);
		// Internalized Names. Mostly important for models & textures
		this.setUnlocalizedName(Lib.MODID + ".chunk_loader");
		this.setRegistryName(new ResourceLocation(Lib.MODID, "chunk_loader"));
		// GameRegistry registration - one for block, one for ItemBlock, one for TE
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this), new ResourceLocation(Lib.MODID, "chunk_loader"));
		GameRegistry.registerTileEntity(TileChunkLoader.class, Lib.MODID + "chunk_loader");
		// Registers the security event if it's enabled
		if (PersistentBits.config.enableSecurity)
			MinecraftForge.EVENT_BUS.register(new Security());
		this.chatUtil = new ChatUtil();
	}

	/**
	 * @author WayofTime
	 * 
	 *         Code borrowed from BloodMagic
	 * 
	 *         Ensures chat notifications aren't spammy
	 */
	public class ChatUtil
	{
		private int lastAdded;

		/**
		 * @param messages
		 *            ITextComponent to be sent, which can be replaced later
		 */
		private void sendNoSpamMessages(ITextComponent... messages)
		{
			GuiNewChat chat = Minecraft.getMinecraft().ingameGUI.getChatGUI();
			for (int i = Lib.CHAT_DEL_ID + messages.length - 1; i <= lastAdded; i++)
			{
				chat.deleteChatLine(i);
			}
			for (int i = 0; i < messages.length; i++)
			{
				chat.printChatMessageWithOptionalDeletion(messages[i], Lib.CHAT_DEL_ID + i);
			}
			lastAdded = Lib.CHAT_DEL_ID + messages.length - 1;
		}
	}
}