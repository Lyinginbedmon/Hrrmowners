package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.reference.Reference;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

public class HOPointOfInterestTypes
{
	public static final List<POI> POI_DATA = Lists.newArrayList();
	
	public static RegistryKey<PointOfInterestType> QUEEN_KEY;
	public static RegistryKey<PointOfInterestType> ARMORER_KEY;
	public static RegistryKey<PointOfInterestType> BUTCHER_KEY;
	public static RegistryKey<PointOfInterestType> CARTOGRAPHER_KEY;
	public static RegistryKey<PointOfInterestType> FARMER_KEY;
	public static RegistryKey<PointOfInterestType> LIBRARIAN_KEY;
	public static RegistryKey<PointOfInterestType> MASON_KEY;
	public static RegistryKey<PointOfInterestType> SHEPHERD_KEY;
	public static RegistryKey<PointOfInterestType> WEAPONSMITH_KEY;
	public static RegistryKey<PointOfInterestType> BROWN_MUSHROOM_KEY, RED_MUSHROOM_KEY;
	
	public static void init()
	{
		POI_DATA.add(new POI(prefix("red_mushroom"), 1, 1, ImmutableSet.of(Blocks.RED_MUSHROOM.getDefaultState())));
		POI_DATA.add(new POI(prefix("brown_mushroom"), 1, 1, ImmutableSet.of(Blocks.BROWN_MUSHROOM.getDefaultState())));
		registerWorkstation("queen", HOBlocks.NEST);
		registerWorkstation("armorer", HOBlocks.BLAST_FURNACE);
		registerWorkstation("butcher", HOBlocks.SMOKER);
		registerWorkstation("cartographer", HOBlocks.CART_TABLE);
		registerWorkstation("farmer", HOBlocks.COMPOSTER);
		registerWorkstation("librarian", HOBlocks.LECTERN);
		registerWorkstation("mason", HOBlocks.STONECUTTER);
		registerWorkstation("shepherd", HOBlocks.LOOM);
		registerWorkstation("weaponsmith", HOBlocks.GRINDSTONE);
		
		if(Platform.isFabric())
			Hrrmowners.HANDLER.registerPOIs();
		else
		{
			final DeferredRegister<PointOfInterestType> POI_REGISTER = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.POINT_OF_INTEREST_TYPE);
			POI_DATA.forEach(poi -> POI_REGISTER.register(poi.id(), () -> poi.make()));
			POI_REGISTER.register();
		}
		
		Hrrmowners.LOGGER.info(" # Registered {} points of interest", POI_DATA.size());
		QUEEN_KEY = key("queen");
		ARMORER_KEY = key("armorer");
		BUTCHER_KEY = key("butcher");
		CARTOGRAPHER_KEY = key("cartographer");
		FARMER_KEY = key("farmer");
		LIBRARIAN_KEY = key("librarian");
		MASON_KEY = key("mason");
		SHEPHERD_KEY = key("shepherd");
		WEAPONSMITH_KEY = key("weaponsmith");
	}
	
	public static RegistryKey<PointOfInterestType> key(String name)
	{
		return RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, prefix(name));
	}
	
	private static void registerWorkstation(String nameIn, RegistrySupplier<Block> blockIn)
	{
		POI_DATA.add(new POI(prefix(nameIn), 1, 1, ImmutableSet.of(blockIn.get().getDefaultState())));
	}
	
	public static record POI(Identifier id, int tickets, int distance, ImmutableSet<BlockState> states)
	{
		public PointOfInterestType make() { return new PointOfInterestType(states, tickets, distance); }
	};
}
