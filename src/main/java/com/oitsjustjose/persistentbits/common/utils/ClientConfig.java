
package com.oitsjustjose.persistentbits.common.utils;

import java.nio.file.Path;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

public class ClientConfig
{
    public static final ForgeConfigSpec CLIENT_CONFIG;
    private static final Builder CLIENT_BUILDER = new Builder();
    public static ForgeConfigSpec.IntValue MAX_INDICATOR_HEIGHT;
    private static final String CATEGORY_CLIENT = "client";

    static
    {
        init();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path)
    {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave()
                .writingMode(WritingMode.REPLACE).build();

        configData.load();
        spec.setConfig(configData);
    }

    public static void init()
    {
        CLIENT_BUILDER.comment("Client-Side Settings").push(CATEGORY_CLIENT);

        MAX_INDICATOR_HEIGHT = CLIENT_BUILDER.comment("The maximum Y-level to show the loaded chunks indicator")
                .defineInRange("maxIndicatorHeight", 128, 0, 255);

        CLIENT_BUILDER.pop();
    }
}