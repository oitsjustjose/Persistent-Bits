package com.oitsjustjose.persistent_bits.blocks;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;
import com.oitsjustjose.persistent_bits.Lib;
import com.oitsjustjose.persistent_bits.PersistentBits;
import com.oitsjustjose.persistent_bits.chunkloading.DetailedCoordinate;
import com.oitsjustjose.persistent_bits.security.Security;
import com.oitsjustjose.persistent_bits.tileentity.TileChunkLoader;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockChunkLoader extends BlockContainer
{
	public BlockChunkLoader()
	{
		super(Material.ROCK);
		this.setHardness(10F);
		this.setResistance(1000F);
		this.setSoundType(SoundType.STONE);
		this.setCreativeTab(CreativeTabs.REDSTONE);
		this.setUnlocalizedName(Lib.MODID + ".chunk_loader");
		this.setRegistryName(new ResourceLocation(Lib.MODID, "chunk_loader"));
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this), new ResourceLocation(Lib.MODID, "chunk_loader"));
		GameRegistry.registerTileEntity(TileChunkLoader.class, Lib.MODID + "chunk_loader");
		if (PersistentBits.config.enableSecurity)
			MinecraftForge.EVENT_BUS.register(new Security());
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

	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileChunkLoader();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		TileChunkLoader chunkTile = (TileChunkLoader) world.getTileEntity(pos);
		if (chunkTile != null && chunkTile.getOwner() != null && !world.isRemote)
			player.addChatMessage(new TextComponentString("This chunk loader is owned by " + chunkTile.getOwner().getName()));
		player.swingArm(EnumHand.MAIN_HAND);
		return true;
	}

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

			PersistentBits.LOGGER.info("Player " + player.getName() + " has placed a Chunk Loader at coordinates: x = " + pos.getX() + ", y = " + pos.getY() + ", z = " + pos.getZ() + " in Dimension " + world.provider.getDimension() + ".");
			PersistentBits.database.addChunkCoord(new DetailedCoordinate(pos, world.provider.getDimension()));
		}

		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
		if (!world.isRemote)
			PersistentBits.database.removeChunkCoord(new DetailedCoordinate(pos, world.provider.getDimension()));
	}
}