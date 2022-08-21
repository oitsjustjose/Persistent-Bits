package com.oitsjustjose.persistentbits.common;

import com.oitsjustjose.persistentbits.common.block.ChunkLoaderBlock;
import com.oitsjustjose.persistentbits.common.utils.Constants;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PersistentBitsRegistry {
    public final DeferredRegister<Item> ItemRegistry = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MODID);

    public final DeferredRegister<Block> BlockRegistry = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MODID);

    public final RegistryObject<ChunkLoaderBlock> chunkLoader;

    public PersistentBitsRegistry() {
        Item.Properties props = new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE).rarity(Rarity.EPIC);
        this.chunkLoader = this.BlockRegistry.register("chunk_loader", ChunkLoaderBlock::new);
        this.ItemRegistry.register("chunk_loader", () -> new BlockItem(chunkLoader.get(), props));
    }
}
