package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.reference.Reference;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

public class HOPointOfInterestTypes
{
	public static final List<POI> POI_DATA = Lists.newArrayList();
	
	public static RegistryKey<PointOfInterestType> NEST_KEY;
	
	public static void init()
	{
		POI_DATA.add(new POI(prefix("nest"), 1, 1, ImmutableSet.of(HOBlocks.NEST.get().getDefaultState())));
		
		if(Platform.isFabric())
			Hrrmowners.HANDLER.registerPOIs();
		else
		{
			final DeferredRegister<PointOfInterestType> POI_REGISTER = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.POINT_OF_INTEREST_TYPE);
			POI_DATA.forEach(poi -> POI_REGISTER.register(poi.id(), () -> poi.make()));
			POI_REGISTER.register();
		}
		
		Hrrmowners.LOGGER.info(" # Registered {} points of interest", POI_DATA.size());
		NEST_KEY = key("nest");
	}
	
	public static RegistryKey<PointOfInterestType> key(String name)
	{
		return RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, prefix(name));
	}
	
	public static record POI(Identifier id, int tickets, int distance, ImmutableSet<BlockState> states)
	{
		public PointOfInterestType make() { return new PointOfInterestType(states, tickets, distance); }
	};
}
