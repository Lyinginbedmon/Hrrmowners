package com.lying.entity.village;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.init.HOVillagePartTypes;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class VillageManager
{
	private static final Logger LOGGER = Hrrmowners.LOGGER;
	private final Map<RegistryKey<World>, List<Village>> villageMap = new HashMap<>();
	
	public VillageManager() { }
	
	public NbtCompound writeToNbt(NbtCompound nbt, ServerWorld world)
	{
		if(villageMap.isEmpty())
			return nbt;
		
		NbtList set = new NbtList();
		for(Entry<RegistryKey<World>, List<Village>> entry : villageMap.entrySet())
		{
			if(entry.getValue().isEmpty())
				continue;
			NbtCompound data = new NbtCompound();
			Identifier.CODEC.encodeStart(NbtOps.INSTANCE, entry.getKey().getValue()).resultOrPartial(LOGGER::error).ifPresent(e -> nbt.put("Dim", e));
			NbtList list = new NbtList();
			entry.getValue().forEach(v -> list.add(v.writeToNbt(new NbtCompound(), world)));
			data.put("Villages", list);
			set.add(data);
		}
		nbt.put("Villages", set);
		return nbt;
	}
	
	public void readFromNbt(NbtCompound nbt, ServerWorld world)
	{
		villageMap.clear();
		
		NbtList set = nbt.getList("Villages", NbtElement.COMPOUND_TYPE);
		for(int i=0; i<set.size(); i++)
		{
			NbtCompound data = set.getCompound(i);
			RegistryKey<World> dimension = World.CODEC.parse(NbtOps.INSTANCE, data.get("Dim")).resultOrPartial(LOGGER::error).orElse(World.OVERWORLD);
			
			List<Village> list = Lists.newArrayList();
			NbtList entries = data.getList("Villages", NbtElement.COMPOUND_TYPE);
			for(int j=0; j<entries.size(); j++)
				list.add(Village.readFromNbt(entries.getCompound(j), world));
			
			villageMap.put(dimension, list);
		}
	}
	
	public boolean createVillage(BlockPos pos, RegistryKey<World> dimension, ServerWorld world)
	{
		while(world.getBlockState(pos).isAir() && pos.getY() > world.getBottomY())
			pos = pos.down();
		
		final BlockPos position = pos;
		RegistryKey<Biome> biome = world.getBiome(position).getKey().get();
		Village village = new Village(UUID.randomUUID(), dimension, biome);
		Random rand = Random.create(pos.getX() * pos.getZ() * pos.getY());
		BlockRotation rotation = BlockRotation.random(rand);
		Optional<VillagePart> partOpt = Village.makeNewPart(position, rotation, world, HOVillagePartTypes.CENTER.get(), HOVillagePartTypes.CENTER.get().getStructurePool(biome), Random.create(position.getX() * position.getZ() * position.getY()));
		if(partOpt.isEmpty())
			return false;
		
		village.addPart(partOpt.get(), world, false);
		return addVillage(dimension, world, village);
	}
	
	public boolean addVillage(RegistryKey<World> dimension, ServerWorld world, Village village)
	{
		List<Village> villages = villageMap.getOrDefault(dimension, Lists.newArrayList());
		if(villages.stream().anyMatch(vil -> vil.id().equals(village.id()) || vil.intersects(village)))
			return false;
		
		village.setInWorld();
		villages.add(village);
		villageMap.put(dimension, villages);
		village.generateAll(world);
		village.notifyObservers();
		return true;
	}
	
	public Optional<Village> getVillage(RegistryKey<World> dimension, BlockPos position)
	{
		List<Village> villages = villageMap.getOrDefault(dimension, Lists.newArrayList());
		if(villages.isEmpty())
			return Optional.empty();
		return villages.stream().filter(v -> v.contains(position)).findFirst();
	}
	
	public Optional<Village> getVillage(UUID id)
	{
		for(List<Village> villages : villageMap.values())
			for(Village village : villages)
				if(village.id().equals(id))
					return Optional.of(village);
		return Optional.empty();
	}
	
	public boolean kill(ServerWorld server, UUID id)
	{
		RegistryKey<World> dim = server.getRegistryKey();
		List<Village> villages = villageMap.getOrDefault(dim, Lists.newArrayList());
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
			villageMap.put(dim, villages);
		return result;
	}
	
	public int killAll(RegistryKey<World> dim, ServerWorld server)
	{
		List<Village> villages = villageMap.getOrDefault(dim, Lists.newArrayList());
		int count = villages.size();
		if(count > 0)
		{
			villages.forEach(village -> village.erase(server));
			villageMap.remove(dim);
		}
		return count;
	}
	
	public void tickVillages(RegistryKey<World> dim, ServerWorld world)
	{
		villageMap.getOrDefault(dim, Lists.newArrayList()).stream().filter(v -> v.isLoaded(world)).forEach(village -> village.tick(world));
	}
}
