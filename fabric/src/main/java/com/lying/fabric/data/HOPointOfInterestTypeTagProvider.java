package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;

import com.lying.init.HOPointOfInterestTypes;
import com.lying.init.HOTags;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.poi.PointOfInterestType;

public class HOPointOfInterestTypeTagProvider extends FabricTagProvider<PointOfInterestType>
{
	public HOPointOfInterestTypeTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture)
	{
		super(output, RegistryKeys.POINT_OF_INTEREST_TYPE, registriesFuture);
	}
	
	protected void configure(WrapperLookup wrapperLookup)
	{
		getOrCreateTagBuilder(HOTags.SURINA_JOB_SITES).add(
				HOPointOfInterestTypes.QUEEN_KEY,
				HOPointOfInterestTypes.ARMORER_KEY,
				HOPointOfInterestTypes.BUTCHER_KEY,
				HOPointOfInterestTypes.CARTOGRAPHER_KEY,
				HOPointOfInterestTypes.FARMER_KEY,
				HOPointOfInterestTypes.LIBRARIAN_KEY,
				HOPointOfInterestTypes.MASON_KEY,
				HOPointOfInterestTypes.SHEPHERD_KEY,
				HOPointOfInterestTypes.WEAPONSMITH_KEY);
	}
}
