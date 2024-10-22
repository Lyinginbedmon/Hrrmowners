package com.lying.data;

import com.lying.reference.Reference;

import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePools;

public class SurinaVillageData
{
	public static final RegistryKey<StructurePool> DESERT_STREET_KEY	= StructurePools.of(Reference.ModInfo.MOD_ID+":village/desert/streets");
	public static final RegistryKey<StructurePool> DESERT_CORNER_KEY	= StructurePools.of(Reference.ModInfo.MOD_ID+":village/desert/corners");
	public static final RegistryKey<StructurePool> DESERT_HOUSE_KEY	= StructurePools.of(Reference.ModInfo.MOD_ID+":village/desert/houses");
	public static final RegistryKey<StructurePool> DESERT_WORK_KEY		= StructurePools.of(Reference.ModInfo.MOD_ID+":village/desert/workstations");
	public static final RegistryKey<StructurePool> DESERT_CENTER_KEY	= StructurePools.of(Reference.ModInfo.MOD_ID+":village/desert/centers");
	
	public static final RegistryKey<StructurePool> BADLANDS_STREET_KEY	= StructurePools.of(Reference.ModInfo.MOD_ID+":village/badlands/streets");
	public static final RegistryKey<StructurePool> BADLANDS_CORNER_KEY	= StructurePools.of(Reference.ModInfo.MOD_ID+":village/badlands/corners");
	public static final RegistryKey<StructurePool> BADLANDS_HOUSE_KEY	= StructurePools.of(Reference.ModInfo.MOD_ID+":village/badlands/houses");
	public static final RegistryKey<StructurePool> BADLANDS_WORK_KEY		= StructurePools.of(Reference.ModInfo.MOD_ID+":village/badlands/workstations");
	public static final RegistryKey<StructurePool> BADLANDS_CENTER_KEY	= StructurePools.of(Reference.ModInfo.MOD_ID+":village/badlands/centers");
}
