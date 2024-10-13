package com.lying.entity.village;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.network.HideCubesPacket;
import com.lying.utility.DebugCuboid;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JigsawBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VillageModel
{
	/** A set of VillagePart reflecting the layout of the village */
	private final List<VillagePart> parts = Lists.newArrayList();
	
	/** A cached list of all unoccupied connection points, as defined by jigsaw blocks*/
	private final List<StructureBlockInfo> connectors = Lists.newArrayList();
	
	public VillageModel copy(NbtCompound nbt, StructureContext context)
	{
		VillageModel model = new VillageModel();
		model.readFromNbt(writeToNbt(new NbtCompound(), context), context);
		return model;
	}
	
	public NbtCompound writeToNbt(NbtCompound nbt, StructureContext context)
	{
		if(!parts.isEmpty())
		{
			NbtList components = new NbtList();
			parts.forEach(p -> components.add(p.writeToNbt(new NbtCompound(), context)));
			nbt.put("Parts", components);
		}
		if(!connectors.isEmpty())
			nbt.put("Connectors", connectorsToNbt(connectors));
		return nbt;
	}
	
	public VillageModel readFromNbt(NbtCompound nbt, StructureContext context)
	{
		parts.clear();
		if(nbt.contains("Parts", NbtElement.LIST_TYPE))
		{
			NbtList components = nbt.getList("Parts", NbtElement.COMPOUND_TYPE);
			for(int i=0; i<components.size(); i++)
				VillagePart.readFromNbt(components.getCompound(i), context.structureTemplateManager(), context).ifPresent(p -> parts.add(p));
		}
		
		connectors.clear();
		if(nbt.contains("Connectors", NbtElement.LIST_TYPE))
			connectors.addAll(nbtToConnectors(nbt.getList("Connectors", NbtElement.COMPOUND_TYPE)));
		
		return this;
	}
	
	public List<VillagePart> parts() { return parts; }
	
	public List<StructureBlockInfo> connectors() { return connectors; }
	
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
	
	public boolean addPart(VillagePart part, ServerWorld world)
	{
		if(parts.stream().anyMatch(part2 -> part2.id.equals(part.id) || part2.bounds.intersects(part.bounds)))
			return false;
		
		parts.add(part);
		recacheConnectors();
		return true;
	}
	
	/** Updates all parts to remove connectors locked by other parts */
	public void recacheConnectors()
	{
		connectors.clear();
		for(VillagePart part : parts)
		{
			List<StructureBlockInfo> remove = Lists.newArrayList();
			for(StructureBlockInfo connector : part.openConnections())
			{
				List<VillagePart> containers = getContainers(connector.pos().offset(JigsawBlock.getFacing(connector.state())));
				if(containers.stream().anyMatch(c -> !c.id.equals(part.id)))
					remove.add(connector);
				else
					connectors.add(connector);
			}
			remove.forEach(info -> part.lockConnectorAt(info.pos()));
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
	
	public List<StructureBlockInfo> getAvailableConnections()
	{
		return parts.stream().flatMap(part -> part.openConnections().stream()).toList();
	}
	
	public void notifyObservers(RegistryKey<World> dimension)
	{
		parts.forEach(part -> part.notifyObservers(dimension));
	}
	
	public static NbtList connectorsToNbt(List<StructureBlockInfo> connectors)
	{
		NbtList set = new NbtList();
		connectors.forEach(p -> 
		{
			NbtCompound data = new NbtCompound();
			data.put("Pos", NbtHelper.fromBlockPos(p.pos()));
			data.put("State", NbtHelper.fromBlockState(p.state()));
			if(!p.nbt().isEmpty())
				data.put("NBT", p.nbt());
			set.add(data);
		});
		return set;
	}
	
	public static List<StructureBlockInfo> nbtToConnectors(NbtList set)
	{
		List<StructureBlockInfo> connectors = Lists.newArrayList();
		for(int i=0; i<set.size(); i++)
		{
			NbtCompound data = set.getCompound(i);
			Optional<BlockPos> pos = NbtHelper.toBlockPos(data, "Pos");
			if(pos.isEmpty())
				continue;
			BlockState state = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), data.getCompound("State"));
			if(state.isAir())
				continue;
			NbtCompound stateData = data.contains("NBT", NbtElement.COMPOUND_TYPE) ? data.getCompound("NBT") : null;
			connectors.add(new StructureBlockInfo(pos.get(), state, stateData));
		}
		return connectors;
	}
}