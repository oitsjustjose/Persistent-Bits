package com.oitsjustjose.persistent_bits.blocks;

import com.oitsjustjose.persistent_bits.Lib;
import com.oitsjustjose.persistent_bits.tileentity.TileChunkLoader;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
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
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileChunkLoader();
	}
}
