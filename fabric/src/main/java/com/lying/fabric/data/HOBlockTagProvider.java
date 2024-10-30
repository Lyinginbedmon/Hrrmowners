package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;

import com.lying.init.HOBlocks;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.BlockTagProvider;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;

public class HOBlockTagProvider extends BlockTagProvider
{
	public HOBlockTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture)
	{
		super(output, registriesFuture);
	}
	
	protected void configure(WrapperLookup wrapperLookup)
	{
		getOrCreateTagBuilder(BlockTags.SAND).add(HOBlocks.SAND_FIRMAMENT.get());
		getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE).add(HOBlocks.SAND_FIRMAMENT.get());
	}
}
