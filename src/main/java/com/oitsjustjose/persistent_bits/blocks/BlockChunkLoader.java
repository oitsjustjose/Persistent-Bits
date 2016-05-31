package com.oitsjustjose.persistent_bits.blocks;

import com.oitsjustjose.persistent_bits.Lib;
import com.oitsjustjose.persistent_bits.PersistentBits;
import com.oitsjustjose.persistent_bits.tileentity.TileChunkLoader;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockChunkLoader extends BlockContainer
{
	public BlockChunkLoader()
	{
		super(Material.rock);
		this.setHardness(10F);
		this.setResistance(1000F);
		this.setStepSound(soundTypeStone);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.7F, 1.0F);
		this.setUnlocalizedName(Lib.MODID + ".chunk_loader");
		this.setCreativeTab(CreativeTabs.tabRedstone);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	public boolean isFullCube()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return 3;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileChunkLoader();
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(world, pos, state, placer, stack);
		if (placer instanceof EntityPlayer && !world.isRemote)
		{
			EntityPlayer player = (EntityPlayer) placer;
			PersistentBits.LOGGER.info("Player " + player.getName() + " has placed a Chunk Loader at coordinates: x = " + pos.getX() + ", y = " + pos.getY() + ", z = " + pos.getZ() + ".");
		}
	}
}
