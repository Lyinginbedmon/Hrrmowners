package com.lying.entity.village;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.network.HideCubesPacket;
import com.lying.utility.DebugCuboid;

import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VillageModel
{
	/** A set of VillagePart reflecting the layout of the village */
	private final List<VillagePart> parts = Lists.newArrayList();
	
	/** A cached list of all unoccupied connection points, as defined by jigsaw blocks*/
	private final List<StructureBlockInfo> connectors = Lists.newArrayList();
	
	public List<VillagePart> parts() { return parts; }
	
	public List<StructureBlockInfo> connectors() { return connectors; }
	
	public boolean isEmpty() { return parts.isEmpty(); }
	
	public boolean cannotExpand() { return connectors.isEmpty(); }
	
	public boolean contains(BlockPos pos) { return parts.stream().anyMatch(part -> part.contains(pos)); }
	
	public boolean wouldIntersect(VillagePart part) { return parts.stream().anyMatch(part2 -> part2.intersects(part)); }
	
	public Optional<VillagePart> getCenter() { return parts.stream().findFirst(); }
	
	public Optional<VillagePart> getPart(UUID id)
	{
		return parts.stream().filter(part -> part.id.equals(id)).findFirst();
	}
	
	public boolean addPart(VillagePart part, ServerWorld world)
	{
		if(parts.stream().anyMatch(part2 -> part2.id.equals(part.id) || part2.bounds.intersects(part.bounds)))
			return false;
		
		parts.add(part);
		connectors.clear();
		connectors.addAll(getAvailableConnections());
		return true;
	}
	
	public void erasePart(VillagePart part, ServerWorld world, RegistryKey<World> dimension)
	{
		List<DebugCuboid> comps = Lists.newArrayList();
		part.collectDebugCuboids(comps);
		for(int x=part.min().getX(); x<=part.max().getX(); x++)
			for(int z=part.min().getZ(); z<=part.max().getZ(); z++)
				for(int y=part.min().getY(); y<=part.max().getY(); y++)
					world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState());
		
		if(!comps.isEmpty())
			Hrrmowners.forAllPlayers(player -> 
			{
				if(!player.getWorld().getRegistryKey().equals(dimension)) return;
				HideCubesPacket.send(player, comps); 
			});
	}
	
	public void eraseAll(ServerWorld world, RegistryKey<World> dimension)
	{
		parts.forEach(part -> erasePart(part, world, dimension));
	}
	
	public List<StructureBlockInfo> getAvailableConnections()
	{
		List<StructureBlockInfo> connectors = Lists.newArrayList();
		parts.forEach(part -> connectors.addAll(part.openConnections()));
		return connectors;
	}
	
	public void notifyObservers(RegistryKey<World> dimension)
	{
		parts.forEach(part -> part.notifyObservers(dimension));
	}
}