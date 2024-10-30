package com.lying.entity.village;

import java.util.function.Function;
import java.util.function.Predicate;

import com.lying.init.HOVillagePartTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.biome.Biome;

public class VillagePartType implements StringIdentifiable
{
	public static final Codec<VillagePartType> CODEC = Identifier.CODEC.comapFlatMap(id -> 
	{
		VillagePartType type = HOVillagePartTypes.byID(id);
		return type == null ? DataResult.error(() -> "Unrecognised part type") : DataResult.success(type); 
	}, VillagePartType::registryName).stable();
	
	private final Identifier regName;
	private final float buildCost;
	private final int colour;
	private final Function<RegistryKey<Biome>, RegistryKey<StructurePool>> structurePool;
	private final Predicate<VillagePartType> connectLogic;
	
	public VillagePartType(Identifier idIn, int col, float cost, Function<RegistryKey<Biome>, RegistryKey<StructurePool>> poolKeyIn)
	{
		regName = idIn;
		buildCost = cost;
		colour = col;
		structurePool = poolKeyIn;
		connectLogic = t -> t == this;
	}
	
	public VillagePartType(Identifier idIn, int col, float cost, Predicate<VillagePartType> connect, Function<RegistryKey<Biome>, RegistryKey<StructurePool>> poolKeyIn)
	{
		regName = idIn;
		buildCost = cost;
		colour = col;
		structurePool = poolKeyIn;
		connectLogic = connect;
	}
	
	public boolean equals(Object b) { return b instanceof VillagePartType && ((VillagePartType)b).regName.equals(regName); }
	
	public boolean equalsAny(Object... array)
	{
		for(Object b : array)
			if(equals(b))
				return true;
		return false;
	}
	
	public Identifier registryName() { return regName; }
	
	/** Returns true if connectors for this type can link to connectors of the given type */
	public boolean canConnectTo(VillagePartType type) { return connectLogic.test(type); }
	
	public int color() { return colour; }
	
	public float costToBuild() { return buildCost; }
	
	public String asString() { return registryName().getPath().toLowerCase(); }
	
	public RegistryKey<StructurePool> getStructurePool(RegistryKey<Biome> biomeIn) { return structurePool.apply(biomeIn); }
}
