package com.lying.entity.village;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.biome.Biome;

public enum PartType implements StringIdentifiable
{
	CENTER(0x0000FF, biome -> StructurePools.ofVanilla("village/desert/town_centers")),
	HOUSE(0x00FF00, biome -> StructurePools.ofVanilla("village/desert/houses")),
	WORK(0xFF0000, biome -> StructurePools.ofVanilla("village/desert/houses")),
	STREET(0xFFFFFF, biome -> StructurePools.ofVanilla("village/desert/streets")),
	CORNER(0xFFFFFF, biome -> StructurePools.ofVanilla("village/desert/streets"));
	
	@SuppressWarnings("deprecation")
	public static final StringIdentifiable.EnumCodec<PartType> CODEC = StringIdentifiable.createCodec(PartType::values);
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
	
	private final int colour;
	private final Function<RegistryKey<Biome>, RegistryKey<StructurePool>> structurePool;
	
	private PartType(int col, Function<RegistryKey<Biome>, RegistryKey<StructurePool>> poolKeyIn)
	{
		colour = col;
		structurePool = poolKeyIn;
	}
	
	public int color() { return colour; }
	
	public String asString() { return name().toLowerCase(); }
	
	@Nullable
	public PartType fromString(String name)
	{
		for(PartType type : values())
			if(type.asString().equalsIgnoreCase(name))
				return type;
		return null;
	}
	
	public RegistryKey<StructurePool> getStructurePool(RegistryKey<Biome> biomeIn) { return structurePool.apply(biomeIn); }
	
	@Nullable
	public static PartType fromPartName(final String name)
	{
		if(name.contains("accessory"))
			return null;
		
		if(name.contains("street"))
			return PartType.STREET;
		else if(WORKSTATION_NAMES.stream().anyMatch(s -> name.contains(s)))
			return PartType.WORK;
		else if(name.contains("house"))
			return PartType.HOUSE;
		
		return null;
	}
}
