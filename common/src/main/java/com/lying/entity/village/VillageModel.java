package com.lying.entity.village;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.entity.village.Village.Resident;
import com.lying.entity.village.ai.Connector;
import com.lying.network.HideCubesPacket;
import com.lying.utility.DebugCuboid;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class VillageModel
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Hrrmowners.LOGGER;
	
	/* Census data representing the village's population */
	private Map<Resident, Integer> census = new HashMap<>();
	private int totalPop = 0;
	
	/** A set of VillagePart reflecting the layout of the village */
	private final List<VillagePartInstance> parts = Lists.newArrayList();
	
	/** A cached list of all unoccupied connection points, as defined by jigsaw blocks*/
	private final List<Connector> connectors = Lists.newArrayList();
	private int connectorIndex = 0;
	
	/** Tally of different types of part within this model */
	private final Map<Identifier, Integer> tally = new HashMap<>();
	
	/** Returns true if the two models are functionally equivalent.<br>They may still differ in arrangement or population. */
	public static boolean isEquivalent(VillageModel modelA, VillageModel modelB)
	{
		if(modelA.connectorIndex != modelB.connectorIndex)
			return false;
		
		if(modelA.connectors.size() == modelB.connectors.size())
		{
			for(Connector connector : modelA.connectors)
				if(modelB.connectors.stream().noneMatch(c -> c.equals(connector)))
					return false;
		}
		else
			return false;
		
		if(modelA.tally.size() == modelB.tally.size())
		{
			for(Entry<Identifier, Integer> entry : modelA.tally.entrySet())
				if(!modelB.tally.containsKey(entry.getKey()) || modelB.tally.get(entry.getKey()) != entry.getValue())
					return false;
		}
		else
			return false;
		
		return true;
	}
	
	/** Tally of all residents */
	public int population() { return totalPop; }
	
	/** Tally of all non-queen residents */
	public int residentPop() { return totalPop - residentsOfType(Resident.QUEEN); }
	
	public int residentsOfType(Resident type) { return Math.max(0, census.getOrDefault(type, 0)); }
	
	public void setResidents(Resident type, int count)
	{
		if(census.containsKey(type))
			totalPop += count - residentsOfType(type);
		
		census.put(type, count);
	}
	
	public void addResident(Resident type, int count)
	{
		int tally = residentsOfType(type);
		count = MathHelper.clamp(count, -tally, Integer.MAX_VALUE);
		census.put(type, tally + count);
		totalPop += count;
	}
	
	public void copyCensus(Village village)
	{
		census.clear();
		totalPop = 0;
		for(Resident type : Resident.values())
			addResident(type, village.getPopulation(type));
	}
	
	public <T extends LivingEntity> List<T> getEnclosedResidents(Class<T> resClass, World world, double expansion)
	{
		List<T> residents = Lists.newArrayList();
		
		// Collect unique entities within the boundaries of this village's component parts
		for(VillagePartInstance part : parts)
			residents.addAll(
				world.getEntitiesByClass(resClass, part.bounds().expand(expansion), Entity::isAlive).stream()
					.filter(l -> residents.stream().noneMatch(l2 -> l.getUuid().equals(l2.getUuid()))).toList());
		return residents;
	}
	
	public VillageModel copy(ServerWorld world)
	{
		return (new VillageModel()).readFromNbt(writeToNbt(new NbtCompound(), world), world);
	}
	
	private void clear()
	{
		parts.clear();
		connectors.clear();
		census.clear();
		totalPop = 0;
	}
	
	public NbtCompound writeToNbt(NbtCompound nbt, ServerWorld world)
	{
		if(totalPop > 0)
		{
			NbtCompound data = new NbtCompound();
			data.putInt("Population", totalPop);
			NbtList tally = new NbtList();
			census.entrySet().stream().filter(e -> e.getValue() > 0).forEach(e -> 
			{
				NbtCompound ent = new NbtCompound();
				ent.putString("Class", e.getKey().asString());
				ent.putInt("Count", e.getValue());
				tally.add(ent);
			});
			data.put("Tally", tally);
			nbt.put("Census", data);
		}
		
		// Since we can't have any connectors or tallies without any parts, just return a blank compound
		if(!parts.isEmpty())
		{
			NbtList components = new NbtList();
			parts.forEach(p -> components.add(p.writeToNbt(new NbtCompound(), StructureContext.from(world))));
			nbt.put("Parts", components);
			
			recacheConnectors(false);
			if(!connectors.isEmpty())
				nbt.put("Connectors", connectorsToNbt(connectors));
		}
		nbt.putInt("SelectedConnector", connectorIndex);
		return nbt;
	}
	
	public VillageModel readFromNbt(NbtCompound nbt, ServerWorld world)
	{
		clear();
		if(nbt.contains("Parts", NbtElement.LIST_TYPE))
		{
			NbtList components = nbt.getList("Parts", NbtElement.COMPOUND_TYPE);
			for(int i=0; i<components.size(); i++)
				VillagePartInstance.readFromNbt(components.getCompound(i), world).ifPresent(p -> {
					parts.add(p);
					tally.put(p.type.registryName(), tally.getOrDefault(p.type.registryName(), 0) + 1);
				});
		}
		
		if(nbt.contains("Connectors", NbtElement.LIST_TYPE))
			connectors.addAll(nbtToConnectors(nbt.getList("Connectors", NbtElement.COMPOUND_TYPE)));
		connectorIndex = nbt.getInt("SelectedConnector");
		
		if(nbt.contains("Census", NbtElement.COMPOUND_TYPE))
		{
			NbtCompound censusData = nbt.getCompound("Census");
			totalPop = censusData.getInt("Population");
			NbtList tally = censusData.getList("Tally", NbtElement.COMPOUND_TYPE);
			for(int i=0; i<tally.size(); i++)
			{
				NbtCompound entry = tally.getCompound(i);
				Resident type = Resident.fromString(entry.getString("Class"));
				if(type != null)
					census.put(type, entry.getInt("Count"));
			}
		}
		
		return this;
	}
	
	public List<VillagePartInstance> parts() { return parts; }
	
	public List<Connector> connectors() { return connectors; }
	
	public int openConnectors() { return connectors.size(); }
	
	public int openConnectors(Predicate<Connector> predicate) { return (int)connectors.stream().filter(predicate).count(); }
	
	public Optional<Connector> selectedConnector()
	{
		int tally = openConnectors();
		if(tally == 0)
			return Optional.empty();
		if(tally == 1)
			return Optional.of(connectors.getFirst());
		
		return Optional.of(connectors.get(connectorIndex % tally));
	}
	
	public Optional<Connector> firstConnectorMatching(Predicate<Connector> predicate)
	{
		return connectors.stream().filter(predicate).findFirst();
	}
	
	public void incSelectedConnector(int inc)
	{
		connectorIndex += inc;
		if(connectorIndex < 0)
			connectorIndex = 0;
	}
	
	public int connectorIndex() { return this.connectorIndex; }
	
	public void selectRandomConnector(Random rand)
	{
		if(openConnectors() < 2)
			return;
		
		connectorIndex = rand.nextBetween(0, openConnectors());
	}
	
	public int getTallyOf(VillagePart type) { return tally.getOrDefault(type.registryName(), 0); }
	
	public int getTallyMatching(Predicate<VillagePartInstance> predicate)
	{
		return (int)parts.stream().filter(predicate).count();
	}
	
	public boolean isEmpty() { return parts.isEmpty(); }
	
	/** Returns true if this model has no available connectors to add new parts */
	public boolean cannotExpand() { return connectors.isEmpty(); }
	
	public boolean contains(BlockPos pos) { return parts.stream().anyMatch(part -> part.contains(pos)); }
	
	/** Returns a list of all parts that contain the given position */
	public List<VillagePartInstance> getContainers(BlockPos pos) { return parts.stream().filter(part -> part.contains(pos)).toList(); }
	
	/** Returns a list of all parts that intersect the given bounding box */
	public List<VillagePartInstance> getIntersections(Box box) { return parts.stream().filter(part -> part.bounds().intersects(box)).toList(); }
	
	public boolean wouldIntersect(VillagePartInstance part) { return parts.stream().anyMatch(part2 -> part2.intersects(part)); }
	
	public Optional<VillagePartInstance> getCenter() { return parts.stream().findFirst(); }
	
	public Optional<VillagePartInstance> getPart(UUID id)
	{
		return parts.stream().filter(part -> part.id.equals(id)).findFirst();
	}
	
	public boolean addPart(VillagePartInstance part, ServerWorld world, boolean shouldNotify)
	{
		if(parts.stream().anyMatch(part2 -> part2.id.equals(part.id) || part2.bounds().intersects(part.bounds())))
			return false;
		
		// Selected connector, prior to updating the list of available connectors
//		Optional<Connector> lastConnector = selectedConnector();
		
		parts.add(part);
		tally.put(part.type.registryName(), tally.getOrDefault(part.type.registryName(), 0) + 1);
		
		recacheConnectors(shouldNotify);
		if(shouldNotify)
			notifyObservers(world.getRegistryKey());
		
		// Move selected connector the closest viable connector
//		if(lastConnector.isPresent())
//		{
//			BlockPos pos = lastConnector.get().pos;
//			int closestInd = 0;
//			double closestDist = Double.MAX_VALUE;
//			for(int index = 0; index < connectors.size(); index++)
//			{
//				double dist = pos.getSquaredDistance(connectors.get(index).pos);
//				if(dist < closestDist)
//				{
//					closestDist = dist;
//					closestInd = index;
//				}
//			}
//			connectorIndex = closestInd;
//		}
		
		// Sort connectors by distance to placed part, this encourages building in the same general area
		BlockPos origin = part.pivot();
		connectors.sort((a, b) -> {
			double disA = a.pos.getSquaredDistance(origin);
			double disB = b.pos.getSquaredDistance(origin);
			return disA < disB ? -1 : disA > disB ? 1 : 0;
		});
		
		return true;
	}
	
	/** Updates all parts to remove connectors locked by other parts */
	public void recacheConnectors(boolean shouldNotify)
	{
		connectors.clear();
		for(VillagePartInstance part : parts)
		{
			Predicate<VillagePartInstance> isNotHost = c -> !c.id.equals(part.id);
			List<Connector> remove = Lists.newArrayList();
			for(Connector connector : part.openConnections())
			{
				// If the connector connects to or would intersect any component other than its host, remove it
				if(getContainers(connector.linkPos()).stream().anyMatch(isNotHost) || getIntersections(connector.occupancy()).stream().anyMatch(isNotHost))
					remove.add(connector);
				else
					connectors.add(connector);
			}
			remove.forEach(info -> part.lockConnectorAt(info.pos, shouldNotify));
		}
	}
	
	public void erasePart(VillagePartInstance part, ServerWorld world, RegistryKey<World> dimension)
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