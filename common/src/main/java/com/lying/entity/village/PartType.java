package com.lying.entity.village;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.StringIdentifiable;

public enum PartType implements StringIdentifiable
{
	CENTER(0x0000FF),
	HOUSE(0x00FF00),
	WORK(0xFF0000),
	STREET(0xFFFFFF);
	
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
	
	private PartType(int col)
	{
		colour = col;
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
