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

		if (config.hasChanged())
			config.save();
	}
}
