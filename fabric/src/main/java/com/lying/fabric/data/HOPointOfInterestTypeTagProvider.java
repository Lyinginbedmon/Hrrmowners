package com.lying.fabric.data;

import java.util.concurrent.CompletableFuture;

import com.lying.data.HOTags;
import com.lying.init.HOPointOfInterestTypes;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

public class HOPointOfInterestTypeTagProvider extends FabricTagProvider<PointOfInterestType>
{
	public HOPointOfInterestTypeTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture)
	{
		super(output, RegistryKeys.POINT_OF_INTEREST_TYPE, registriesFuture);
	}
	
	protected void configure(WrapperLookup wrapperLookup)
	{
		getOrCreateTagBuilder(HOTags.SURINA_JOB_SITES).add(
				PointOfInterestTypes.ARMORER, 
				PointOfInterestTypes.BUTCHER, 
				PointOfInterestTypes.CARTOGRAPHER, 
				PointOfInterestTypes.CLERIC, 
				PointOfInterestTypes.FARMER, 
				PointOfInterestTypes.FISHERMAN, 
				PointOfInterestTypes.FLETCHER, 
				PointOfInterestTypes.LEATHERWORKER, 
				PointOfInterestTypes.LIBRARIAN, 
				PointOfInterestTypes.MASON, 
				PointOfInterestTypes.SHEPHERD, 
				PointOfInterestTypes.TOOLSMITH, 
				PointOfInterestTypes.WEAPONSMITH,
				HOPointOfInterestTypes.NEST_KEY);
	}
}
