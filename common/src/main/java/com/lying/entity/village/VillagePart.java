package com.lying.entity.village;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.network.ShowCubesPacket;
import com.lying.utility.DebugCuboid;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class VillagePart
{
	public final UUID id;
	
	public final PartType type;
	public final BlockRotation rotation;
	public final BlockBox bounds;
	
	/** Part IDs that this part is linked to */
	private final List<UUID> linksTo = Lists.newArrayList();
	/** Open connections remaining for this part */
	private final List<JigsawJunction> connectors = Lists.newArrayList();
	
	private final PoolStructurePiece piece;
	
	public VillagePart(UUID idIn, PartType typeIn, PoolStructurePiece pieceIn)
	{
		id = idIn;
		type = typeIn;
		piece = pieceIn;
		connectors.addAll(piece.getJunctions());
		rotation = piece.getRotation();
		bounds = piece.getBoundingBox();
	}
	
	public boolean addLinkTo(VillagePart partIn)
	{
		if(linksTo.contains(partIn.id))
			return false;
		
		linksTo.add(partIn.id);
		// TODO Remove any connectors that would intersect this link
		
		return true;
	}
	
	public boolean hasOpenSlots() { return !connectors.isEmpty(); }
	
	public List<JigsawJunction> openSlots() { return connectors; }
	
	public void placeInWorld(ServerWorld world)
	{
		piece.generate(world, world.getStructureAccessor(), world.getChunkManager().getChunkGenerator(), random(), bounds, pivot(), true);
	}
	
	public List<JigsawJunction> connectors() { return connectors; }
	
	public Random random() { return Random.create(pivot().getX() * pivot().getZ() * pivot().getY()); }
	
	public BlockPos pivot() { return bounds.getCenter(); }
	
	public BlockPos min() { return new BlockPos(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ()); }
	public BlockPos max() { return new BlockPos(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ()); }
	
	public boolean contains(BlockPos pos) { return bounds.contains(pos); }
	public boolean intersects(VillagePart part) { return bounds.intersects(part.bounds); }
	
	public void notifyObservers(RegistryKey<World> dimension)
	{
		List<DebugCuboid> comps = Lists.newArrayList();
		collectDebugCuboids(comps);
		
		if(!comps.isEmpty())
			Hrrmowners.forAllPlayers(player -> 
			{
				if(!player.getWorld().getRegistryKey().equals(dimension)) return;
				ShowCubesPacket.send(player, comps); 
			});
	}
	
	public void collectDebugCuboids(List<DebugCuboid> collection)
	{
		collection.add(new DebugCuboid(min(), max(), type, ""));
		
		// FIXME Identify actual global coordinates of all jigsaw blocks in the structure
		System.out.println("Notifying players of "+connectors.size()+" connectors");
		connectors.forEach(junction -> 
		{
			BlockPos pos = new BlockPos(junction.getSourceX(), junction.getDeltaY(), junction.getSourceZ()).add(piece.getPos());
			collection.add(new DebugCuboid(pos, pos, PartType.WORK, "junction"));
		});
	}
}