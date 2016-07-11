package com.oitsjustjose.persistent_bits.blocks;

import com.oitsjustjose.persistent_bits.Lib;
import com.oitsjustjose.persistent_bits.PersistentBits;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockChunkLoader extends BlockContainer
{
    private final AxisAlignedBB AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.7F, 1.0F);
	
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
	}
	
	@Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
       return AABB;
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
			PersistentBits.LOGGER.info("Player " + player.getName() + " has placed a Chunk Loader at coordinates: x = " + pos.getX() + ", y = " + pos.getY() + ", z = " + pos.getZ() + " in Dimension " + player.dimension + ".");
		}
	}
}
