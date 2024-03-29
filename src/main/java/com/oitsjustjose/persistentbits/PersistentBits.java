package com.oitsjustjose.persistentbits;

import com.oitsjustjose.persistentbits.common.block.ChunkLoaderBlock;
import com.oitsjustjose.persistentbits.common.capability.ChunkLoaderList;
import com.oitsjustjose.persistentbits.common.capability.IChunkLoaderList;
import com.oitsjustjose.persistentbits.common.utils.ClientConfig;
import com.oitsjustjose.persistentbits.common.utils.CommonConfig;
import com.oitsjustjose.persistentbits.common.utils.Constants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Constants.MODID)
public class PersistentBits {
    private static PersistentBits instance;
    public Logger LOGGER = LogManager.getLogger();

    public final ChunkLoaderBlock CHUNKLOADER = new ChunkLoaderBlock();

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
//        CapabilityManager.INSTANCE.register(IChunkLoaderList.class, new ChunkLoaderList.Storage(),
//                () -> new ChunkLoaderList(null));
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            blockRegistryEvent.getRegistry().register(PersistentBits.getInstance().CHUNKLOADER);
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
            BlockItem loaderAsItem = new BlockItem(PersistentBits.getInstance().CHUNKLOADER,
                    new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS));
            loaderAsItem.setRegistryName(ChunkLoaderBlock.REGISTRY_NAME);
            itemRegistryEvent.getRegistry().register(loaderAsItem);
        }
    }

    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(ChunkLoaderList.class);
    }

    @SubscribeEvent
    public void attachWorldCaps(AttachCapabilitiesEvent<Level> event) {
        if (event.getObject().isClientSide()) {
            return;
        }

        try {

            final LazyOptional<IChunkLoaderList> inst = LazyOptional
                    .of(() -> new ChunkLoaderList((ServerLevel) event.getObject()));

            final ICapabilitySerializable<CompoundTag> provider = new ICapabilitySerializable<CompoundTag>() {
                @Override
                public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
                    return ChunkLoaderList.CAPABILITY.orEmpty(cap, inst);
                }

                @Override
                public CompoundTag serializeNBT() {
                    IChunkLoaderList cap = this.getCapability(ChunkLoaderList.CAPABILITY).orElse(null);
                    return cap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundTag nbt) {
                    IChunkLoaderList cap = this.getCapability(ChunkLoaderList.CAPABILITY).orElse(null);
                    cap.deserializeNBT(nbt);
                }
            };
            event.addCapability(ChunkLoaderBlock.REGISTRY_NAME, provider);
            event.addListener(() -> inst.invalidate());
        } catch (Exception e) {
            PersistentBits.getInstance().LOGGER.error("PersistentBits has faced a fatal error. The game will crash...");
            // PersistentBits.getInstance().LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
