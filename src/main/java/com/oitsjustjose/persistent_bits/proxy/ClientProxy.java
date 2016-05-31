package com.oitsjustjose.persistent_bits.proxy;

/**
 * @author oitsjustjose with asiekierka's help (and others)
 * 
 * This code is free to be used to help you out. Just change the assignment of the variables
 * MODID and tab to be your proper MODID and CreativeTab, then this class will work for you!
 * 
 * NOTE: It is assumed that you initialize your items' / blocks' unlocalized names using
 * .setUnlocalizedName(MODID + ".<youDesiredNameHere>"); - this is critical for function below 
 * 
 * Item models will need to be placed in assets/<your_modid_all_lowercase_no_spaces>/models/items
 * Block models will need to be placed in assets/<your_modid_all_lowercase_no_spaces>/models/blocks
 * Block models will need BlockState files too, in assets/<your_modid_all_lowercase_no_spaces>/blockstates
 * 
 */

import java.util.ArrayList;
import java.util.List;

import com.oitsjustjose.persistent_bits.Lib;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientProxy extends CommonProxy
{
	static CreativeTabs tab = CreativeTabs.tabRedstone;
	static String MODID = Lib.MODID;

	/**
	 * @param item
	 *            The Item to register a model registry for. You still have to make the model file, but now MC will know where to look
	 */
	@SideOnly(Side.CLIENT)
	public static void register(Item item)
	{
		int meta = 0;

		List<ItemStack> subItems = new ArrayList<ItemStack>();
		item.getSubItems(item, tab, subItems);
		for (ItemStack sub : subItems)
		{
			String name = item.getUnlocalizedName(sub).substring(MODID.length() + 6).toLowerCase();
			ModelBakery.registerItemVariants(item, new ResourceLocation(MODID.toLowerCase(), name));
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, new ModelResourceLocation(MODID.toLowerCase() + ":" + name, "inventory"));
			meta++;
		}

	}

	/**
	 * @param block
	 *            The Item to register a model registry for. You still have to make the model file, but now MC will know where to look
	 */
	@SideOnly(Side.CLIENT)
	public static void register(Block block)
	{
		int meta = 0;
		Item itemBlock = Item.getItemFromBlock(block);
		// Checks if the block has metadata / subtypes
		if (itemBlock.getHasSubtypes())
		{
			List<ItemStack> subItems = new ArrayList<ItemStack>();
			itemBlock.getSubItems(itemBlock, tab, subItems);
			for (ItemStack sub : subItems)
			{
				String name = itemBlock.getUnlocalizedName(sub).toLowerCase().replace(MODID.toLowerCase() + ".", "").replace("tile.", "");
				ModelBakery.registerItemVariants(itemBlock, new ResourceLocation(MODID.toLowerCase(), name));
				Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(itemBlock, meta, new ModelResourceLocation(MODID.toLowerCase() + ":" + name, "inventory"));
				meta++;
			}
		}
		else
		{
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(itemBlock, 0, new ModelResourceLocation(MODID.toLowerCase() + ":" + block.getUnlocalizedName().substring(MODID.length() + 6).toLowerCase(), "inventory"));
		}
	}
}