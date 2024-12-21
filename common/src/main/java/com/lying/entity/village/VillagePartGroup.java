package com.lying.entity.village;

import java.util.function.Predicate;

import com.lying.init.HOVillagePartGroups;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.util.Identifier;

public class VillagePartGroup 
{
	public static final Codec<VillagePartGroup> CODEC = Identifier.CODEC.comapFlatMap(id -> 
	{
		VillagePartGroup type = HOVillagePartGroups.byID(id);
		return type == null ? DataResult.error(() -> "Unrecognised part group") : DataResult.success(type); 
	}, VillagePartGroup::registryName).stable();
	
	private final Identifier regName;
	private final int colour;
	private final Predicate<VillagePartGroup> connectLogic;
	
	public VillagePartGroup(Identifier idIn, int col)
	{
		regName = idIn;
		colour = col;
		connectLogic = t -> is(t);
	}
	
	public VillagePartGroup(Identifier idIn, int col, Predicate<VillagePartGroup> connect)
	{
		regName = idIn;
		colour = col;
		connectLogic = connect;
	}
	
	public boolean equals(Object b) { return b instanceof VillagePartGroup && is((VillagePartGroup)b); }
	
	public boolean is(VillagePartGroup b) { return b.regName.equals(this.regName); }
	
	public boolean equalsAny(Object... array)
	{
		for(Object b : array)
			if(equals(b))
				return true;
		return false;
	}
	
	public Identifier registryName() { return regName; }
	
	/** Returns true if connectors for this type can link to connectors of the given type */
	public boolean canConnectTo(VillagePartGroup type) { return connectLogic.test(type); }
	
	public int color() { return colour; }
	
	public String asString() { return registryName().getPath().toLowerCase(); }
}
