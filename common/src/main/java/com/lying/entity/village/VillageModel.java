package com.lying.entity.village;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.entity.village.ai.Connector;
import com.lying.network.HideCubesPacket;
import com.lying.utility.DebugCuboid;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VillageModel
{
	/** A set of VillagePart reflecting the layout of the village */
	private final List<VillagePart> parts = Lists.newArrayList();
	
	/** A cached list of all unoccupied connection points, as defined by jigsaw blocks*/
	private final List<Connector> connectors = Lists.newArrayList();
	
	/** Tally of different types of part within this model */
	private final Map<PartType, Integer> tally = new HashMap<>();
	
	public boolean isEquivalent(VillageModel model)
	{
		if(connectors.size() == model.connectors.size())
		{
			for(Connector connector : connectors)
				if(model.connectors.stream().noneMatch(c -> c.equals(connector)))
					return false;
		}
		else
			return false;
		
		if(tally.size() == model.tally.size())
		{
			for(Entry<PartType, Integer> entry : tally.entrySet())
				if(!model.tally.containsKey(entry.getKey()) || model.tally.get(entry.getKey()) != entry.getValue())
					return false;
		}
		else
			return false;
		
		return true;
	}
	
	public VillageModel copy(ServerWorld world)
	{
		VillageModel model = new VillageModel();
		model.readFromNbt(writeToNbt(new NbtCompound(), world), world);
		return model;
	}
	
	public NbtCompound writeToNbt(NbtCompound nbt, ServerWorld world)
	{
		// Since we can't have any connectors or tallies without any parts, just return a blank compound
		if(parts.isEmpty())
			return nbt;
		else
		{
			NbtList components = new NbtList();
			parts.forEach(p -> components.add(p.writeToNbt(new NbtCompound(), StructureContext.from(world))));
			nbt.put("Parts", components);
		}
		
		if(!connectors.isEmpty())
			nbt.put("Connectors", connectorsToNbt(connectors));
		return nbt;
	}
	
	public VillageModel readFromNbt(NbtCompound nbt, ServerWorld world)
	{
		parts.clear();
		if(nbt.contains("Parts", NbtElement.LIST_TYPE))
		{
			NbtList components = nbt.getList("Parts", NbtElement.COMPOUND_TYPE);
			for(int i=0; i<components.size(); i++)
				VillagePart.readFromNbt(components.getCompound(i), world).ifPresent(p -> {
					parts.add(p);
					tally.put(p.type, tally.getOrDefault(p.type, 0) + 1);
				});
		}
		
		connectors.clear();
		if(nbt.contains("Connectors", NbtElement.LIST_TYPE))
			connectors.addAll(nbtToConnectors(nbt.getList("Connectors", NbtElement.COMPOUND_TYPE)));
		
		return this;
	}
	
	public List<VillagePart> parts() { return parts; }
	
	public List<Connector> connectors() { return connectors; }
	
	public int getCount(PartType type) { return tally.getOrDefault(type, 0); }
	
	public boolean isEmpty() { return parts.isEmpty(); }
	
	public boolean cannotExpand() { return connectors.isEmpty(); }
	
	public boolean contains(BlockPos pos) { return parts.stream().anyMatch(part -> part.contains(pos)); }
	
	public List<VillagePart> getContainers(BlockPos pos) { return parts.stream().filter(part -> part.contains(pos)).toList(); }
	
	public boolean wouldIntersect(VillagePart part) { return parts.stream().anyMatch(part2 -> part2.intersects(part)); }
	
	public Optional<VillagePart> getCenter() { return parts.stream().findFirst(); }
	
	public Optional<VillagePart> getPart(UUID id)
	{
		return parts.stream().filter(part -> part.id.equals(id)).findFirst();
	}
	
	public boolean addPart(VillagePart part, ServerWorld world, boolean shouldNotify)
	{
		if(parts.stream().anyMatch(part2 -> part2.id.equals(part.id) || part2.bounds.intersects(part.bounds)))
			return false;
		
		parts.add(part);
		tally.put(part.type, tally.getOrDefault(part.type, 0) + 1);
		
		recacheConnectors(shouldNotify);
		if(shouldNotify)
			notifyObservers(world.getRegistryKey());
		return true;
	}
	
	/** Updates all parts to remove connectors locked by other parts */
	public void recacheConnectors(boolean shouldNotify)
	{
		connectors.clear();
		for(VillagePart part : parts)
		{
			List<Connector> remove = Lists.newArrayList();
			for(Connector connector : part.openConnections())
			{
				List<VillagePart> containers = getContainers(connector.linkPos());
				if(containers.stream().anyMatch(c -> !c.id.equals(part.id)))
					remove.add(connector);
				else
					connectors.add(connector);
			}
			remove.forEach(info -> part.lockConnectorAt(info.pos, shouldNotify));
		}
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
	
	public List<Connector> getAvailableConnections()
	{
		return parts.stream().flatMap(part -> part.openConnections().stream()).toList();
	}
	
	public void notifyObservers(RegistryKey<World> dimension)
	{
		parts.forEach(part -> part.notifyObservers(dimension));
	}
	
	public static NbtList connectorsToNbt(List<Connector> connectors)
	{
		NbtList set = new NbtList();
		connectors.forEach(p -> set.add(p.toNbt()));
		return set;
	}
	
	public static List<Connector> nbtToConnectors(NbtList set)
	{
		List<Connector> connectors = Lists.newArrayList();
		for(int i=0; i<set.size(); i++)
		{
			Connector connector = Connector.fromNbt(set.getCompound(i));
			if(connector != null)
				connectors.add(connector);
		}
		return connectors;
	}
}