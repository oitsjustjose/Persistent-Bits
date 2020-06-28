package com.oitsjustjose.persistentbits;

import com.oitsjustjose.persistentbits.common.block.ChunkLoaderBlock;
import com.oitsjustjose.persistentbits.common.block.MiniLoaderBlock;
import com.oitsjustjose.persistentbits.common.capability.ChunkLoaderList;
import com.oitsjustjose.persistentbits.common.capability.IChunkLoaderList;
import com.oitsjustjose.persistentbits.common.capability.IMiniLoaderList;
import com.oitsjustjose.persistentbits.common.capability.MiniLoaderList;
import com.oitsjustjose.persistentbits.common.utils.ClientConfig;
import com.oitsjustjose.persistentbits.common.utils.CommonConfig;
import com.oitsjustjose.persistentbits.common.utils.Constants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemGroup;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(Constants.MODID)
public class PersistentBits {
    private static PersistentBits instance;
    public Logger LOGGER = LogManager.getLogger();

    @CapabilityInject(IChunkLoaderList.class)
    public static Capability<IChunkLoaderList> CAPABILITY = null;

    @CapabilityInject(IMiniLoaderList.class)
    public static Capability<IMiniLoaderList> MINI_CAPABILITY = null;

    public final ChunkLoaderBlock CHUNKLOADER = new ChunkLoaderBlock();
    public final MiniLoaderBlock MINILOADER = new MiniLoaderBlock();

    public PersistentBits() {
        instance = this;

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);

        this.configSetup();
    }

    public static PersistentBits getInstance() {
        return instance;
    }

    private void configSetup() {
        ModLoadingContext.get().registerConfig(Type.CLIENT, ClientConfig.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(Type.COMMON, CommonConfig.COMMON_CONFIG);
        CommonConfig.loadConfig(CommonConfig.COMMON_CONFIG,
                FMLPaths.CONFIGDIR.get().resolve("persistentbits-common.toml"));
    }

    public void setup(final FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(IChunkLoaderList.class, new ChunkLoaderList.Storage(),
                () -> new ChunkLoaderList(null));
        CapabilityManager.INSTANCE.register(IMiniLoaderList.class, new MiniLoaderList.Storage(),
                () -> new MiniLoaderList(null));
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            blockRegistryEvent.getRegistry().register(PersistentBits.getInstance().CHUNKLOADER);
            blockRegistryEvent.getRegistry().register(PersistentBits.getInstance().MINILOADER);
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
            BlockItem loaderAsItem = new BlockItem(PersistentBits.getInstance().CHUNKLOADER,
                    new Properties().group(ItemGroup.DECORATIONS));
            loaderAsItem.setRegistryName(ChunkLoaderBlock.REGISTRY_NAME);
            itemRegistryEvent.getRegistry().register(loaderAsItem);

            BlockItem miniLoaderAsItem = new BlockItem(PersistentBits.getInstance().MINILOADER,
                    new Properties().group(ItemGroup.DECORATIONS));
            miniLoaderAsItem.setRegistryName(MiniLoaderBlock.REGISTRY_NAME);
            itemRegistryEvent.getRegistry().register(miniLoaderAsItem);
        }
    }

    @SubscribeEvent
    public void attachWorldCaps(AttachCapabilitiesEvent<World> event) {
        if (event.getObject().isRemote) {
            return;
        }

        final LazyOptional<IChunkLoaderList> inst = LazyOptional
                .of(() -> new ChunkLoaderList((ServerWorld) event.getObject()));
        final ICapabilitySerializable<INBT> provider = new ICapabilitySerializable<INBT>() {
            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
                return CAPABILITY.orEmpty(cap, inst);
            }

            @Override
            public INBT serializeNBT() {
                return CAPABILITY.writeNBT(inst.orElse(null), null);
            }

            @Override
            public void deserializeNBT(INBT nbt) {
                CAPABILITY.readNBT(inst.orElse(null), null, nbt);
            }
        };
        event.addCapability(ChunkLoaderBlock.REGISTRY_NAME, provider);
        event.addListener(() -> inst.invalidate());

        /*
         * -----------------------------------------------------------------------------
         */

        final LazyOptional<IMiniLoaderList> miniInst = LazyOptional
                .of(() -> new MiniLoaderList((ServerWorld) event.getObject()));
        final ICapabilitySerializable<INBT> miniProvider = new ICapabilitySerializable<INBT>() {
            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
                return MINI_CAPABILITY.orEmpty(cap, miniInst);
            }

            @Override
            public INBT serializeNBT() {
                return MINI_CAPABILITY.writeNBT(miniInst.orElse(null), null);
            }

            @Override
            public void deserializeNBT(INBT nbt) {
                MINI_CAPABILITY.readNBT(miniInst.orElse(null), null, nbt);
            }
        };
        event.addCapability(MiniLoaderBlock.REGISTRY_NAME, miniProvider);
        event.addListener(() -> miniInst.invalidate());
    }
}
