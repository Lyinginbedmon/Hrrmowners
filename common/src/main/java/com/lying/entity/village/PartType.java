package com.lying.entity.village;

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
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.biome.Biome;

public class PartType implements StringIdentifiable
{
	public static final Codec<PartType> CODEC = Identifier.CODEC.comapFlatMap(id -> 
	{
		PartType type = PartType.byID(id);
		return type == null ? DataResult.error(() -> "Unrecognised part type") : DataResult.success(type); 
	}, PartType::registryName).stable();
	
	private static final Map<Identifier, Supplier<PartType>> VALUES = new HashMap<>();
	
	private static final Predicate<PartType> IS_STREET_OR_CORNER = t -> t.equalsAny(PartType.STREET.get(), PartType.CORNER.get());
	private static final Predicate<PartType> IS_BIG_BUILDING = t -> t.equalsAny(PartType.HOUSE.get(), PartType.WORK.get());
	
	public static final Supplier<PartType> CENTER	= register(prefix("center"), id -> new PartType(id, 0x0000FF, 5F, biome -> SurinaVillageData.DESERT_CENTER_KEY));
	public static final Supplier<PartType> HOUSE	= register(prefix("house"), id -> new PartType(id, 0x00FF00, 2F, IS_BIG_BUILDING, biome -> SurinaVillageData.DESERT_HOUSE_KEY));
	public static final Supplier<PartType> WORK		= register(prefix("workstation"), id -> new PartType(id, 0xFF0000, 2F, IS_BIG_BUILDING, biome -> SurinaVillageData.DESERT_WORK_KEY));
	public static final Supplier<PartType> STREET	= register(prefix("street"), id -> new PartType(id, 0xFFFFFF, 1F, IS_STREET_OR_CORNER, biome -> SurinaVillageData.DESERT_STREET_KEY));
	public static final Supplier<PartType> CORNER	= register(prefix("corner"), id -> new PartType(id, 0xFFFFFF, 1F, IS_STREET_OR_CORNER, biome -> SurinaVillageData.DESERT_CORNER_KEY));
	
	private static Supplier<PartType> register(Identifier name, Function<Identifier, PartType> funcIn)
	{
		Supplier<PartType> supplier = () -> funcIn.apply(name);
		VALUES.put(name, supplier);
		return supplier;
	}
	
	public static void init()
	{
		Hrrmowners.LOGGER.info(" # Initialised {} village part types", VALUES.size());
	}
	
	public static List<PartType> values() { return VALUES.values().stream().map(t -> t.get()).toList(); }
	
	@Nullable
	public static PartType byID(Identifier idIn) { return VALUES.getOrDefault(idIn, () -> null).get(); }
	
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
	public static PartType fromPartName(final String name)
	{
		if(name.contains("accessory"))
			return null;
		
		if(name.contains("street"))
			return PartType.STREET.get();
		else if(WORKSTATION_NAMES.stream().anyMatch(s -> name.contains(s)))
			return PartType.WORK.get();
		else if(name.contains("house"))
			return PartType.HOUSE.get();
		
		return null;
	}
	
	private final Identifier regName;
	private final float buildCost;
	private final int colour;
	private final Function<RegistryKey<Biome>, RegistryKey<StructurePool>> structurePool;
	private final Predicate<PartType> connectLogic;
	
	private PartType(Identifier idIn, int col, float cost, Function<RegistryKey<Biome>, RegistryKey<StructurePool>> poolKeyIn)
	{
		regName = idIn;
		buildCost = cost;
		colour = col;
		structurePool = poolKeyIn;
		connectLogic = t -> t == this;
	}
	
	private PartType(Identifier idIn, int col, float cost, Predicate<PartType> connect, Function<RegistryKey<Biome>, RegistryKey<StructurePool>> poolKeyIn)
	{
		regName = idIn;
		buildCost = cost;
		colour = col;
		structurePool = poolKeyIn;
		connectLogic = connect;
	}
	
	public boolean equals(Object b) { return b instanceof PartType && ((PartType)b).regName.equals(regName); }
	
	public boolean equalsAny(Object... array)
	{
		for(Object b : array)
			if(equals(b))
				return true;
		return false;
	}
	
	public Identifier registryName() { return regName; }
	
	/** Returns true if connectors for this type can link to connectors of the given type */
	public boolean canConnectTo(PartType type) { return connectLogic.test(type); }
	
	public int color() { return colour; }
	
	public float costToBuild() { return buildCost; }
	
	public String asString() { return registryName().getPath().toLowerCase(); }
	
	public RegistryKey<StructurePool> getStructurePool(RegistryKey<Biome> biomeIn) { return structurePool.apply(biomeIn); }
}
