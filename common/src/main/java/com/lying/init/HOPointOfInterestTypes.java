package com.lying.init;

import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.lying.Hrrmowners;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.poi.PointOfInterestType;

public class HOPointOfInterestTypes
{
	public static final DeferredRegister<PointOfInterestType> POIs = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.POINT_OF_INTEREST_TYPE);
	private static int tally = 0;
	
	public static final RegistryKey<PointOfInterestType> NEST_KEY = key("nest");
	public static final RegistrySupplier<PointOfInterestType> NEST	= register(NEST_KEY, () -> new PointOfInterestType(ImmutableSet.of(HOBlocks.NEST.get().getDefaultState()), 1, 1));
	
	private static RegistrySupplier<PointOfInterestType> register(String nameIn, Supplier<PointOfInterestType> blockIn)
	{
		return register(key(nameIn), blockIn);
	}
	
	private static RegistrySupplier<PointOfInterestType> register(RegistryKey<PointOfInterestType> nameIn, Supplier<PointOfInterestType> blockIn)
	{
		++tally;
		return POIs.register(nameIn.getValue(), blockIn);
	}
	
	public static void init()
	{
		POIs.register();
		Hrrmowners.LOGGER.info(" # Registered {} points of interest", tally);
	}
	
	public static RegistryKey<PointOfInterestType> key(String name)
	{
		return RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, Reference.ModInfo.prefix(name));
	}
}
