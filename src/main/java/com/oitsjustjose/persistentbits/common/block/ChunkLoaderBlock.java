package com.oitsjustjose.persistentbits.common.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.oitsjustjose.persistentbits.PersistentBits;
import com.oitsjustjose.persistentbits.common.utils.Constants;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class ChunkLoaderBlock extends Block
{
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Constants.MODID, "chunk_loader");

    public ChunkLoaderBlock()
    {
        super(Properties.create(Material.ROCK).hardnessAndResistance(10F, 1000F).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE).harvestLevel(2));
        this.setRegistryName(REGISTRY_NAME);
    }

    @Override
    public boolean isSolid(BlockState state)
    {
        return false;
    }

    @Override
    @Nonnull
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        return VoxelShapes.create(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    }

    // @Override
    // public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
    // BlockRayTraceResult hit)
    // {
    // return true;
    // }

    @Override
    @Nonnull
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack)
    {
        if (world.isRemote)
        {
            return;
        }
        world.getCapability(PersistentBits.CAPABILITY, null).ifPresent(cap -> cap.add(pos));
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (world.isRemote)
        {
            return;
        }
        world.getCapability(PersistentBits.CAPABILITY, null).ifPresent(cap -> cap.remove(pos));
    }
}