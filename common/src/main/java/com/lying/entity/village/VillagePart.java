package com.lying.entity.village;

import java.util.function.Function;

import com.lying.init.HOVillageParts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.biome.Biome;

public class VillagePart implements StringIdentifiable
{
	public static final Codec<VillagePart> CODEC = Identifier.CODEC.comapFlatMap(id -> 
	{
		VillagePart type = HOVillageParts.byID(id);
		return type == null ? DataResult.error(() -> "Unrecognised part type") : DataResult.success(type); 
	}, VillagePart::registryName).stable();
	
	private final Identifier regName;
	private final VillagePartGroup group;
	
	private final float buildCost;
	private final Function<RegistryKey<Biome>, RegistryKey<StructurePool>> structurePool;
	
	public VillagePart(Identifier idIn, VillagePartGroup groupIn, float cost, Function<RegistryKey<Biome>, RegistryKey<StructurePool>> poolKeyIn)
	{
		regName = idIn;
		group = groupIn;
		buildCost = cost;
		structurePool = poolKeyIn;
	}
	
	public boolean equals(Object b) { return b instanceof VillagePart && ((VillagePart)b).regName.equals(regName); }
	
	public boolean equalsAny(Object... array)
	{
		for(Object b : array)
			if(equals(b))
				return true;
		return false;
	}
	
	public Identifier registryName() { return regName; }
	
	public VillagePartGroup group() { return group; }
	
	public float costToBuild() { return buildCost; }
	
	public String asString() { return registryName().getPath().toLowerCase(); }
	
	public RegistryKey<StructurePool> getStructurePool(RegistryKey<Biome> biomeIn) { return structurePool.apply(biomeIn); }
}
