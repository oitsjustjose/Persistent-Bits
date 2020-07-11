package com.oitsjustjose.persistentbits.common.block;

import com.oitsjustjose.persistentbits.PersistentBits;
import com.oitsjustjose.persistentbits.common.utils.ClientConfig;
import com.oitsjustjose.persistentbits.common.utils.CommonConfig;
import com.oitsjustjose.persistentbits.common.utils.Constants;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ChunkLoaderBlock extends Block implements IWaterLoggable {
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Constants.MODID, "chunk_loader");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public ChunkLoaderBlock() {
        super(Properties.create(Material.ROCK).hardnessAndResistance(10F, 1000F).sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE).harvestLevel(2).notSolid());
        this.setRegistryName(REGISTRY_NAME);
        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public PushReaction getPushReaction(@Nonnull BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (context.getWorld().getBlockState(context.getPos()).getBlock() == Blocks.WATER) {
            return this.getDefaultState().with(WATERLOGGED, Boolean.TRUE);
        }
        return this.getDefaultState();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public IFluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(@Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos,
                                boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        // Update the water from flowing to still or vice-versa
        if (state.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
        return VoxelShapes.create(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    }

    @Nonnull
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(@Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, PlayerEntity player,
                                             @Nonnull Hand handIn, @Nonnull BlockRayTraceResult hit) {
        player.sendStatusMessage(new TranslationTextComponent("block.persistentbits.chunk_loader.showing.range"), true);
        showVisualization(worldIn, pos);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer,
                                @Nonnull ItemStack stack) {
        showVisualization(world, pos);
        if (world.isRemote) {
            return;
        }
        world.getCapability(PersistentBits.CAPABILITY, null).ifPresent(cap -> cap.add(pos));
        if (CommonConfig.ENABLE_LOGGING.get()) {
            PersistentBits.getInstance().LOGGER.info("Chunk Loader placed in chunk [{}, {}]", pos.getX() >> 4,
                    pos.getZ() >> 4);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(@Nonnull BlockState state, World world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (world.isRemote) {
            return;
        }
        world.getCapability(PersistentBits.CAPABILITY, null).ifPresent(cap -> cap.remove(pos));
        if (CommonConfig.ENABLE_LOGGING.get()) {
            PersistentBits.getInstance().LOGGER.info("Chunk Loader removed in chunk [{}, {}]", pos.getX() >> 4,
                    pos.getZ() >> 4);
        }
    }

    public List<ChunkPos> getLoadArea(BlockPos pos) {
        ArrayList<ChunkPos> ret = new ArrayList<>();
        ChunkPos chunkPos = new ChunkPos(pos);
        int radius = CommonConfig.LOADING_RADIUS.get();

        for (int x = chunkPos.x - radius; x < chunkPos.x + radius; x++) {
            for (int z = chunkPos.z - radius; z < chunkPos.z + radius; z++) {
                ret.add(new ChunkPos(x, z));
            }
        }

        return ret;
    }

    /**
     * Toggles an in-world representation of which chunks are loaded
     *
     * @param world Block
     * @param pos   Block
     */
    public void showVisualization(World world, BlockPos pos) {
        if (world.isRemote) {
            List<BlockPos> chunkCenters = new ArrayList<>();
            List<ChunkPos> area = this.getLoadArea(pos);

            area.forEach((chunkPos) -> chunkCenters.add(new BlockPos(((chunkPos.x << 4) + 8), pos.getY(), (chunkPos.z << 4) + 8)));

            for (BlockPos p : chunkCenters) {
                for (int i = 0; p.up(i).getY() < p.getY() + ClientConfig.MAX_INDICATOR_HEIGHT.get(); i++) {
                    world.addParticle(ParticleTypes.END_ROD, true, p.up(i).getX(), p.up(i).getY(), p.up(i).getZ(), 0, 0,
                            0);
                }
            }
        }
    }
}