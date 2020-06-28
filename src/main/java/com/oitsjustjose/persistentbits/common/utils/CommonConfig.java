package com.oitsjustjose.persistentbits.common.utils;

import java.nio.file.Path;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommonConfig {
    public static final ForgeConfigSpec COMMON_CONFIG;
    private static final Builder COMMON_BUILDER = new Builder();
    public static ForgeConfigSpec.BooleanValue ENABLE_LOGGING;
    public static ForgeConfigSpec.IntValue LOADING_RADIUS;
    private static String CHUNK_LOADING = "chunk loading";

    static {
        init();
        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave()
                .writingMode(WritingMode.REPLACE).build();

        configData.load();
        spec.setConfig(configData);
    }

    private static void init() {
        COMMON_BUILDER.comment("chunk loader settings").push(CHUNK_LOADING);
        ENABLE_LOGGING = COMMON_BUILDER.comment("Enable verbose logging of placements and removals of chunk loaders")
                .define("verboseLoaders", false);
        LOADING_RADIUS = COMMON_BUILDER.comment("The radius (in chunks) that the chunk loader will cover")
                .defineInRange("loadingRadius", 3, 1, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();
    }
}
