package com.oitsjustjose.persistent_bits;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Config
{
	public Configuration config;
	public boolean enableSecurity;
	public boolean enableNotification;
	public int radius;
	public int maxHeightIndicator;
	public String chunkIndicator;

	public Config(File configFile)
	{
		this.init(configFile);
	}

	void init(File configFile)
	{
		if (config == null)
		{
			config = new Configuration(configFile);
			loadConfiguration();
		}
	}

	void loadConfiguration()
	{
		Property property = config.get(Configuration.CATEGORY_GENERAL, "Enable Chunk Loader Security", false);
		property.setComment("Enabling this means player that don't own a chunk loader can't break it");
		enableSecurity = property.getBoolean();

		property = config.get(Configuration.CATEGORY_GENERAL, "Enable Console Notification of Placement / Destruction", true);
		property.setComment("Helpful for servers to see when someone has placed a Chunk Loader, and where");
		enableNotification = property.getBoolean();

		property = config.get(Configuration.CATEGORY_GENERAL, "Chunk Loading Radius", 3);
		property.setComment("The radius of chunks covered by the loader");
		radius = property.getInt();

		property = config.get(Configuration.CATEGORY_CLIENT, "Max Height for Loaded Chunk Indicator", 24, "", 0, 255);
		property.setComment("This is how many blocks above the current Y level the loaded chunk indicators will pillar up to");
		maxHeightIndicator = property.getInt();

		property = config.get(Configuration.CATEGORY_CLIENT, "Loaded Chunk Indicator Block", "minecraft:stained_glass_pane:14");
		property.setComment("The Block to be used as the loaded chunk indicator. Can be any block. Formatted as <modid>:<registryName> or <modid>:<registryName>:<meta>");
		chunkIndicator = property.getString();

		if (config.hasChanged())
			config.save();
	}
}
