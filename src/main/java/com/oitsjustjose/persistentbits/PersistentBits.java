package com.oitsjustjose.persistentbits;

import com.oitsjustjose.persistentbits.common.PersistentBitsRegistry;
import com.oitsjustjose.persistentbits.common.capability.ChunkLoaderList;
import com.oitsjustjose.persistentbits.common.capability.IChunkLoaderList;
import com.oitsjustjose.persistentbits.common.utils.ClientConfig;
import com.oitsjustjose.persistentbits.common.utils.CommonConfig;
import com.oitsjustjose.persistentbits.common.utils.Constants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(Constants.MODID)
public class PersistentBits {
    private static PersistentBits instance;
    public Logger LOGGER = LogManager.getLogger();
    public static PersistentBitsRegistry Registry = new PersistentBitsRegistry();

    public PersistentBits() {
        instance = this;
        MinecraftForge.EVENT_BUS.register(this);
        Registry.ItemRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
        Registry.BlockRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
        this.configSetup();
    }

    public static PersistentBits getInstance() {
        return instance;
    }

    private void configSetup() {
        ModLoadingContext.get().registerConfig(Type.CLIENT, ClientConfig.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(Type.COMMON, CommonConfig.COMMON_CONFIG);
        CommonConfig.loadConfig(CommonConfig.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("persistentbits-common.toml"));
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

            final LazyOptional<IChunkLoaderList> inst = LazyOptional.of(() -> new ChunkLoaderList((ServerLevel) event.getObject()));

            final ICapabilitySerializable<CompoundTag> provider = new ICapabilitySerializable<CompoundTag>() {
                @Override
                public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
                    return ChunkLoaderList.CAPABILITY.orEmpty(cap, inst);
                }

                @Override
                public CompoundTag serializeNBT() {
                    try {
                        IChunkLoaderList cap = this.getCapability(ChunkLoaderList.CAPABILITY).orElseThrow(RuntimeException::new);
                        return cap.serializeNBT();
                    } catch (RuntimeException ex) {
                        LOGGER.error("Persistent Bits failed to get its Capability from the Server Level. Things may go badly..");
                        ex.printStackTrace();
                        return new CompoundTag();
                    }
                }

                @Override
                public void deserializeNBT(CompoundTag nbt) {
                    try {
                        IChunkLoaderList cap = this.getCapability(ChunkLoaderList.CAPABILITY).orElseThrow(RuntimeException::new);
                        cap.deserializeNBT(nbt);
                    } catch (RuntimeException ex) {
                        LOGGER.error("Persistent Bits failed to get its Capability from the Server Level. Things may go badly..");
                        ex.printStackTrace();
                    }
                }
            };
            event.addCapability(ForgeRegistries.BLOCKS.getKey(Registry.chunkLoader), provider);
            event.addListener(inst::invalidate);
        } catch (Exception e) {
            PersistentBits.getInstance().LOGGER.error("PersistentBits has faced a fatal error. The game will crash...");
            PersistentBits.getInstance().LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
