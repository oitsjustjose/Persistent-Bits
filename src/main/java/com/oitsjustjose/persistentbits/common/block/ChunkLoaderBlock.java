package com.oitsjustjose.persistentbits.common.block;

import com.mojang.math.Vector3f;
import com.oitsjustjose.persistentbits.PersistentBits;
import com.oitsjustjose.persistentbits.common.capability.ChunkLoaderList;
import com.oitsjustjose.persistentbits.common.utils.ClientConfig;
import com.oitsjustjose.persistentbits.common.utils.CommonConfig;
import com.oitsjustjose.persistentbits.common.utils.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ChunkLoaderBlock extends Block implements SimpleWaterloggedBlock {
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Constants.MODID, "chunk_loader");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public ChunkLoaderBlock() {
        super(Properties.of(Material.STONE)
                .strength(50.0F, 1200.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
                .dynamicShape()
        );
        this.setRegistryName(REGISTRY_NAME);
        this.registerDefaultState(this.getStateDefinition().any().setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public PushReaction getPistonPushReaction(BlockState s) {
        return PushReaction.DESTROY;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos()).getBlock() == Blocks.WATER) {
            return this.defaultBlockState().setValue(WATERLOGGED, Boolean.TRUE);
        }
        return this.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        // Update the water from flowing to still or vice-versa
        if (state.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.create(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    }

    @Nonnull
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        player.displayClientMessage(new TranslatableComponent("block.persistentbits.chunk_loader.showing.range"), true);
        showVisualization(worldIn, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean removedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        world.getCapability(ChunkLoaderList.CAPABILITY, null).ifPresent(cap -> cap.remove(pos));
        if (CommonConfig.ENABLE_LOGGING.get()) {
            PersistentBits.getInstance().LOGGER.info("Chunk Loader removed in chunk [{}, {}]", pos.getX() >> 4,
                    pos.getZ() >> 4);
        }

        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        showVisualization(world, pos);
        if (world.isClientSide()) {
            return;
        }
        world.getCapability(ChunkLoaderList.CAPABILITY, null).ifPresent(cap -> cap.add(pos));
        if (CommonConfig.ENABLE_LOGGING.get()) {
            PersistentBits.getInstance().LOGGER.info("Chunk Loader placed in chunk [{}, {}]", pos.getX() >> 4,
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
    public void showVisualization(Level world, BlockPos pos) {
        if (world.isClientSide()) {
            List<BlockPos> chunkCenters = new ArrayList<>();
            List<ChunkPos> area = this.getLoadArea(pos);

            ParticleOptions particle = new DustParticleOptions(new Vector3f(43/255F, 166/255F, 139/255F), 1F);

            area.forEach((chunkPos) -> chunkCenters.add(new BlockPos(((chunkPos.x << 4) + 8), pos.getY(), (chunkPos.z << 4) + 8)));

            for (BlockPos p : chunkCenters) {
                for (int i = 0; p.above(i).getY() < p.getY() + ClientConfig.MAX_INDICATOR_HEIGHT.get(); i++) {
                    world.addParticle(particle, true, p.above(i).getX(), p.above(i).getY(), p.above(i).getZ(), 0, 0,
                            0);
                }
            }
        }
    }
}