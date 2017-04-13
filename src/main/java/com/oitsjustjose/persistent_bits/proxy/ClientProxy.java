package com.oitsjustjose.persistent_bits.proxy;

import com.oitsjustjose.persistent_bits.Lib;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientProxy extends CommonProxy
{
	static CreativeTabs tab = CreativeTabs.REDSTONE;
	static String MODID = Lib.MODID;

	/**
	 * @param item
	 *            The Item to register a model registry for. You still have to make the model file, but now MC will know where to look
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void register(Item item)
	{
		int meta = 0;

		NonNullList<ItemStack> subItems = NonNullList.create();
		item.getSubItems(item, tab, subItems);
		for (ItemStack sub : subItems)
		{
			String name = item.getUnlocalizedName(sub).substring(6).toLowerCase();
			ModelBakery.registerItemVariants(item, new ResourceLocation(MODID, name));
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, new ModelResourceLocation(MODID + ":" + name, "inventory"));
			meta++;
		}

	}

	/**
	 * @param block
	 *            The Item to register a model registry for. You still have to make the model file, but now MC will know where to look
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void register(Block block)
	{
		int meta = 0;
		ItemBlock itemBlock = new ItemBlock(block);
		// Checks if the block has metadata / subtypes
		if (itemBlock.getHasSubtypes())
		{
			NonNullList<ItemStack> subItems = NonNullList.create();
			itemBlock.getSubItems(itemBlock, tab, subItems);
			for (ItemStack sub : subItems)
			{
				String name = itemBlock.getUnlocalizedName(sub).toLowerCase().replace(MODID + ".", "").replace("tile.", "");
				ModelBakery.registerItemVariants(itemBlock, new ResourceLocation(MODID, name));
				Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(itemBlock, meta, getLocation(block));
				meta++;
			}
		}
		else
		{
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(itemBlock, 0, getLocation(block));
		}
	}

	private ModelResourceLocation getLocation(Block block)
	{
		return new ModelResourceLocation(MODID + ":" + block.getUnlocalizedName().substring(6).toLowerCase(), "inventory");
	}
}