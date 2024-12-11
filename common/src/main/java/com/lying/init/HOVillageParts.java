package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.data.SurinaVillageData;
import com.lying.entity.village.VillagePart;
import com.lying.entity.village.VillagePartGroup;

import net.minecraft.util.Identifier;

public class HOVillageParts
{
	private static final Map<Identifier, Supplier<VillagePart>> VALUES = new HashMap<>();
	
	public static final Supplier<VillagePart> CENTER	= register(prefix("center"), id -> new VillagePart(id, HOVillagePartGroups.CENTER.get(), 5F, biome -> SurinaVillageData.DESERT_CENTER_KEY));
	
	public static final Supplier<VillagePart> HOUSE		= register(prefix("house"), id -> new VillagePart(id, HOVillagePartGroups.HOUSE.get(), 2F, biome -> SurinaVillageData.DESERT_HOUSE_KEY));
	
	public static final Supplier<VillagePart> STREET	= register(prefix("street"), id -> new VillagePart(id, HOVillagePartGroups.STREET.get(), 1F, biome -> SurinaVillageData.DESERT_STREET_KEY));
	public static final Supplier<VillagePart> CORNER	= register(prefix("corner"), id -> new VillagePart(id, HOVillagePartGroups.STREET.get(), 1F, biome -> SurinaVillageData.DESERT_CORNER_KEY));
	
	public static final Supplier<VillagePart> WEAPONSMITH	= register(prefix("weaponsmith"), id -> new VillagePart(id, HOVillagePartGroups.WORK.get(), 3F, biome -> SurinaVillageData.DESERT_WORK_KEY));
	public static final Supplier<VillagePart> CARTOGRAPHER	= register(prefix("cartographer"), id -> new VillagePart(id, HOVillagePartGroups.WORK.get(), 3F, biome -> SurinaVillageData.DESERT_WORK_KEY));
	public static final Supplier<VillagePart> FARM			= register(prefix("farm"), id -> new VillagePart(id, HOVillagePartGroups.WORK.get(), 3F, biome -> SurinaVillageData.DESERT_WORK_KEY));
	public static final Supplier<VillagePart> ARMORER		= register(prefix("armorer"), id -> new VillagePart(id, HOVillagePartGroups.WORK.get(), 3F, biome -> SurinaVillageData.DESERT_WORK_KEY));
	public static final Supplier<VillagePart> BUTCHER		= register(prefix("butcher"), id -> new VillagePart(id, HOVillagePartGroups.WORK.get(), 3F, biome -> SurinaVillageData.DESERT_WORK_KEY));
	public static final Supplier<VillagePart> FISHER		= register(prefix("fisher"), id -> new VillagePart(id, HOVillagePartGroups.WORK.get(), 3F, biome -> SurinaVillageData.DESERT_WORK_KEY));
	public static final Supplier<VillagePart> LIBRARY		= register(prefix("library"), id -> new VillagePart(id, HOVillagePartGroups.WORK.get(), 3F, biome -> SurinaVillageData.DESERT_WORK_KEY));
	public static final Supplier<VillagePart> MASON			= register(prefix("mason"), id -> new VillagePart(id, HOVillagePartGroups.WORK.get(), 3F, biome -> SurinaVillageData.DESERT_WORK_KEY));
	public static final Supplier<VillagePart> SHEPHERD		= register(prefix("shepherd"), id -> new VillagePart(id, HOVillagePartGroups.WORK.get(), 3F, biome -> SurinaVillageData.DESERT_WORK_KEY));
	public static final Supplier<VillagePart> TANNER		= register(prefix("tanner"), id -> new VillagePart(id, HOVillagePartGroups.WORK.get(), 3F, biome -> SurinaVillageData.DESERT_WORK_KEY));
	
	private static Supplier<VillagePart> register(Identifier name, Function<Identifier, VillagePart> funcIn)
	{
		Supplier<VillagePart> supplier = () -> funcIn.apply(name);
		VALUES.put(name, supplier);
		return supplier;
	}
	
	public static void init()
	{
		Hrrmowners.LOGGER.info(" # Initialised {} village part types", VALUES.size());
	}
	
	public static List<VillagePart> values() { return VALUES.values().stream().map(t -> t.get()).toList(); }
	
	@Nullable
	public static VillagePart byID(Identifier idIn) { return VALUES.getOrDefault(idIn, () -> null).get(); }
	
	public static List<VillagePart> ofGroup(VillagePartGroup group)
	{
		List<VillagePart> parts = Lists.newArrayList();
		VALUES.values().stream().filter(p -> p.get().group().equals(group)).forEach(p -> parts.add(p.get()));
		return parts;
	}
	
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
	public static VillagePart fromPartName(final String name)
	{
		if(name.contains("accessory"))
			return null;
		
		if(name.contains("street"))
			return STREET.get();
		else if(WORKSTATION_NAMES.stream().anyMatch(s -> name.contains(s)))
			return WEAPONSMITH.get();
		else if(name.contains("house"))
			return HOUSE.get();
		
		return null;
	}
}
