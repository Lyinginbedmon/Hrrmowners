package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;

import com.lying.init.HOBlocks;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class HOBlockLootTableProvider extends FabricBlockLootTableProvider
{
	public HOBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<WrapperLookup> registryLookup)
	{
		super(dataOutput, registryLookup);
	}
	
	public void generate()
	{
		addDrop(HOBlocks.SAND_FIRMAMENT.get());
	}
}
