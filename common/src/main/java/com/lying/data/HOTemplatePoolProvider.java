package com.lying.data;

import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.lying.Hrrmowners;
import com.lying.reference.Reference;
import com.mojang.datafixers.util.Pair;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.pool.StructurePool.Projection;

public class HOTemplatePoolProvider
{
	public HOTemplatePoolProvider(Registerable<StructurePool> context)
	{
		Hrrmowners.LOGGER.info("# Generated structure template pools");
		
		RegistryEntryLookup<StructurePool> lookup = context.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
		RegistryEntry.Reference<StructurePool> registry = lookup.getOrThrow(StructurePools.EMPTY);
		context.register(SurinaVillageData.DESERT_STREET_KEY, new StructurePool(registry, ImmutableList.of(
				create("village/desert/streets/straight_01", 1), 
				create("village/desert/streets/straight_02", 1), 
				create("village/desert/streets/t_junction", 1), 
				create("village/desert/streets/crossroads", 1)), StructurePool.Projection.TERRAIN_MATCHING));
		context.register(SurinaVillageData.DESERT_CORNER_KEY, new StructurePool(registry, ImmutableList.of(
				create("village/desert/corneres/corner_01", 1),
				create("village/desert/corneres/corner_02", 1),
				create("village/desert/corneres/corner_03", 1)
				), StructurePool.Projection.TERRAIN_MATCHING));
	}
	
	private static Pair<Function<Projection, ? extends StructurePoolElement>, Integer> create(String name, int weight)
	{
		return Pair.of(StructurePoolElement.ofSingle(Reference.ModInfo.MOD_ID+":"+name), weight);
	}
}
