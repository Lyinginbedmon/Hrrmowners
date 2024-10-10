package com.lying.entity.village;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
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
		while(world.getBlockState(pos).isAir() && pos.getY() > world.getBottomY())
			pos = pos.down();
		
		final BlockPos position = pos;
		Village village = new Village(UUID.randomUUID(), dimension, world.getBiome(position));
		Random rand = Random.create(pos.getX() * pos.getZ() * pos.getY());
		BlockRotation rotation = BlockRotation.random(rand);
		Optional<VillagePart> partOpt = Village.makeNewPart(position, rotation, world, PartType.CENTER, PartType.CENTER.getStructurePool(world.getBiome(position)), Random.create(position.getX() * position.getZ() * position.getY()));
		if(partOpt.isEmpty())
			return false;
		
		village.addPart(partOpt.get(), world, false);
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
	
	public Optional<Village> getVillage(RegistryKey<World> dimension, BlockPos position)
	{
		List<Village> villages = VILLAGES.getOrDefault(dimension, Lists.newArrayList());
		if(villages.isEmpty())
			return Optional.empty();
		return villages.stream().filter(v -> v.contains(position)).findFirst();
	}
	
	public boolean kill(ServerWorld server, UUID id)
	{
		RegistryKey<World> dim = server.getRegistryKey();
		List<Village> villages = VILLAGES.getOrDefault(dim, Lists.newArrayList());
		if(villages.isEmpty())
			return false;
		
		boolean result = villages.removeIf(v -> 
		{
			boolean bl = v.id().equals(id);
			if(bl)
				v.erase(server);
			return bl;
		});
		if(result)
			VILLAGES.put(dim, villages);
		return result;
	}
	
	public int killAll(RegistryKey<World> dim, ServerWorld server)
	{
		List<Village> villages = VILLAGES.getOrDefault(dim, Lists.newArrayList());
		int count = villages.size();
		if(count > 0)
		{
			villages.forEach(village -> village.erase(server));
			VILLAGES.remove(dim);
		}
		return count;
	}
	
	public void tickVillages(RegistryKey<World> dim, ServerWorld world)
	{
		VILLAGES.getOrDefault(dim, Lists.newArrayList()).stream().filter(v -> v.isLoaded(world)).forEach(village -> village.tick(world));
	}
}
