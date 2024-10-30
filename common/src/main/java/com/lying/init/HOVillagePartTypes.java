package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.lying.Hrrmowners;
import com.lying.data.SurinaVillageData;
import com.lying.entity.village.VillagePartType;

import net.minecraft.util.Identifier;

public class HOVillagePartTypes
{
	private static final Map<Identifier, Supplier<VillagePartType>> VALUES = new HashMap<>();
	
	public static final Supplier<VillagePartType> CENTER	= register(prefix("center"), id -> new VillagePartType(id, 0x0000FF, 5F, biome -> SurinaVillageData.DESERT_CENTER_KEY));
	public static final Supplier<VillagePartType> HOUSE		= register(prefix("house"), id -> new VillagePartType(id, 0x00FF00, 2F, isBigBuilding(), biome -> SurinaVillageData.DESERT_HOUSE_KEY));
	public static final Supplier<VillagePartType> WORK		= register(prefix("workstation"), id -> new VillagePartType(id, 0xFF0000, 2F, isBigBuilding(), biome -> SurinaVillageData.DESERT_WORK_KEY));
	public static final Supplier<VillagePartType> STREET	= register(prefix("street"), id -> new VillagePartType(id, 0xFFFFFF, 1F, isStreetOrCorner(), biome -> SurinaVillageData.DESERT_STREET_KEY));
	public static final Supplier<VillagePartType> CORNER	= register(prefix("corner"), id -> new VillagePartType(id, 0xFFFFFF, 1F, isStreetOrCorner(), biome -> SurinaVillageData.DESERT_CORNER_KEY));
	
	private static Supplier<VillagePartType> register(Identifier name, Function<Identifier, VillagePartType> funcIn)
	{
		Supplier<VillagePartType> supplier = () -> funcIn.apply(name);
		VALUES.put(name, supplier);
		return supplier;
	}
	
	private static Predicate<VillagePartType> isStreetOrCorner() { return t -> t.equalsAny(STREET.get(), CORNER.get()); }
	
	private static Predicate<VillagePartType> isBigBuilding() { return t -> t.equalsAny(HOUSE.get(), WORK.get()); }
	
	public static void init()
	{
		Hrrmowners.LOGGER.info(" # Initialised {} village part types", VALUES.size());
	}
	
	public static List<VillagePartType> values() { return VALUES.values().stream().map(t -> t.get()).toList(); }
	
	@Nullable
	public static VillagePartType byID(Identifier idIn) { return VALUES.getOrDefault(idIn, () -> null).get(); }
	
	private static final List<String> WORKSTATION_NAMES = List.of(
			"smith", 
			"cartographer", 
			"farm",
			"armorer",
			"butcher",
			"fisher",
			"library",
			"mason",
			"shepherd",
			"tanner");
	
	@Nullable
	public static VillagePartType fromPartName(final String name)
	{
		if(name.contains("accessory"))
			return null;
		
		if(name.contains("street"))
			return STREET.get();
		else if(WORKSTATION_NAMES.stream().anyMatch(s -> name.contains(s)))
			return WORK.get();
		else if(name.contains("house"))
			return HOUSE.get();
		
		return null;
	}
}
