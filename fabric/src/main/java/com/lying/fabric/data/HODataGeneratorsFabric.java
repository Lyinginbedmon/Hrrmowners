package com.lying.fabric.data;

import com.lying.data.HOTemplatePoolProvider;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class HODataGeneratorsFabric implements DataGeneratorEntrypoint
{
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator)
	{
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(HOBlockLootTableProvider::new);
		pack.addProvider(HOBlockTagProvider::new);
		pack.addProvider(HOPointOfInterestTypeTagProvider::new);
	}
	
	public void buildRegistry(RegistryBuilder registryBuilder)
	{
		registryBuilder.addRegistry(RegistryKeys.TEMPLATE_POOL, HOTemplatePoolProvider::new);
	}
}