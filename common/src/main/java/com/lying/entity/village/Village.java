package com.lying.entity.village;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.entity.SurinaEntity;
import com.lying.network.HideCubesPacket;
import com.lying.utility.DebugCuboid;

import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Village
{
	private final UUID id;
	private final RegistryKey<World> dimension;
	
	/** True if this village exists in the world yet */
	private boolean inWorld = false;
	
	/** Cached array of residents within the boundaries of the village */
	private final List<SurinaEntity> residents = Lists.newArrayList();
	
	/** A set of VillagePart reflecting the layout of the village */
	private final List<VillagePart> model = Lists.newArrayList();
	
	public Village(UUID idIn, RegistryKey<World> dimIn)
	{
		id = idIn;
		dimension = dimIn;
	}
	
	public UUID id() { return this.id; }
	
	public void setInWorld() { this.inWorld = true; }
	
	@SuppressWarnings("deprecation")
	public boolean isLoaded(World world)
	{
		if(!inWorld || getCenter().isEmpty())
			return false;
		
		VillagePart core = getCenter().get();
		return world.isChunkLoaded(core.min()) && world.isChunkLoaded(core.max());
	}
	
	public void tick(ServerWorld world)
	{
		// Occasionally add a random new part to the village model
	}
	
	public void erase(ServerWorld world)
	{
		List<DebugCuboid> comps = Lists.newArrayList();
		for(VillagePart part : model)
		{
			part.collectDebugCuboids(comps);
			
			for(int x=part.min().getX(); x<=part.max().getX(); x++)
				for(int z=part.min().getZ(); z<=part.max().getZ(); z++)
					for(int y=part.min().getY(); y<=part.max().getY(); y++)
						world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState());
		}
		
		if(!comps.isEmpty())
			Hrrmowners.forAllPlayers(player -> 
			{
				if(!player.getWorld().getRegistryKey().equals(dimension)) return;
				HideCubesPacket.send(player, comps); 
			});
	}
	
	public void notifyObservers()
	{
		if(!inWorld) return;
		
		model.forEach(part -> part.notifyObservers(dimension));
	}
	
	public boolean intersects(Village village)
	{
		for(VillagePart part : model)
		{
			if(village.model.stream().anyMatch(part2 -> part2.intersects(part)))
				return true;
		}
		return false;
	}
	
	public boolean contains(BlockPos pos)
	{
		return model.stream().anyMatch(part -> part.contains(pos));
	}
	
	public void generateAll(ServerWorld world)
	{
		model.forEach(part -> part.placeInWorld(world));
	}
	
	public Optional<VillagePart> getCenter() { return model.stream().findFirst(); }
	
	public Optional<VillagePart> getPart(UUID id)
	{
		return model.stream().filter(part -> part.id.equals(id)).findFirst();
	}
	
	public boolean addPart(VillagePart part, ServerWorld world)
	{
		if(model.stream().anyMatch(part2 -> part2.id.equals(part.id) || part2.bounds.intersects(part.bounds)))
			return false;
		
		model.add(part);
		if(inWorld)
		{
			part.notifyObservers(world.getRegistryKey());
			part.placeInWorld(world);
		}
		return true;
	}
}
