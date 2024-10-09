package com.lying.entity.village;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.DesertVillageData;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class VillageManager
{
	private final Map<RegistryKey<World>, List<Village>> VILLAGES = new HashMap<>();
	
	public VillageManager() { }
	
	public boolean createVillage(BlockPos pos, RegistryKey<World> dimension, ServerWorld world)
	{
		Village village = new Village(UUID.randomUUID(), dimension);
		
		while(world.getBlockState(pos).isAir() && pos.getY() > world.getBottomY())
			pos = pos.down();
		
		final BlockPos position = pos;
		Random rand = Random.create(pos.getX() * pos.getZ() * pos.getY());
		BlockRotation rotation = BlockRotation.random(rand);
		village.addPart(makeNewPart(position, rotation, world, DesertVillageData.TOWN_CENTERS_KEY), world);
		
		return addVillage(dimension, world, village);
	}
	
	public boolean addVillage(RegistryKey<World> dimension, ServerWorld world, Village village)
	{
		List<Village> villages = VILLAGES.getOrDefault(dimension, Lists.newArrayList());
		if(villages.stream().anyMatch(vil -> vil.id().equals(village.id()) || vil.intersects(village)))
			return false;
		
		village.setInWorld();
		villages.add(village);
		VILLAGES.put(dimension, villages);
		village.generateAll(world);
		village.notifyObservers();
		return true;
	}
	
	public void killAll(ServerWorld server)
	{
		VILLAGES.getOrDefault(server.getRegistryKey(), Lists.newArrayList()).forEach(village -> village.erase(server));
	}
	
	public static VillagePart makeNewPart(final BlockPos position, final BlockRotation rotation, ServerWorld server, RegistryKey<StructurePool> poolKey)
	{
		DynamicRegistryManager registryManager = server.getRegistryManager();
		Registry<StructurePool> registry = registryManager.get(RegistryKeys.TEMPLATE_POOL);
		StructurePoolAliasLookup aliasLookup = StructurePoolAliasLookup.create(List.of(), position, server.getSeed());
		Optional<StructurePool> optPool = Optional.of(poolKey).flatMap(key -> registry.getOrEmpty(aliasLookup.lookup(key)));
		if(optPool.isEmpty())
			return null;
		
		Random rand = Random.create(position.getX() * position.getZ() * position.getY());
		StructureTemplateManager manager = server.getStructureTemplateManager();
		StructurePoolElement element = optPool.get().getRandomElement(rand);
		PoolStructurePiece poolStructurePiece = new PoolStructurePiece(
				manager, 
				element, 
				position, 
				element.getGroundLevelDelta(), 
				rotation, 
				element.getBoundingBox(manager, position, rotation), 
				StructureLiquidSettings.APPLY_WATERLOGGING);
		
		return new VillagePart(UUID.randomUUID(), PartType.CENTER, poolStructurePiece);
	}
	
	public void tickVillages(RegistryKey<World> dim, ServerWorld world)
	{
		VILLAGES.getOrDefault(dim, Lists.newArrayList()).stream().filter(v -> v.isLoaded(world)).forEach(village -> village.tick(world));
	}
}
