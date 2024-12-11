package com.lying.entity.village;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.entity.village.ai.Connector;
import com.lying.init.HOVillageParts;
import com.lying.network.HideCubesPacket;
import com.lying.network.ShowCubesPacket;
import com.lying.utility.DebugCuboid;

import net.minecraft.block.Blocks;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class VillagePartInstance
{
	private static final Identifier JIGSAW = Identifier.ofVanilla("jigsaw");
	protected static final Logger LOGGER = Hrrmowners.LOGGER;
	
	public final UUID id;
	
	public final VillagePart type;
	public final BlockRotation rotation;
	private BlockBox bounds;
	
	/** Open connections remaining for this part */
	private final List<Connector> connectors = Lists.newArrayList();
	
	private final PoolStructurePiece piece;
	
	public VillagePartInstance(UUID idIn, VillagePart typeIn, PoolStructurePiece pieceIn, StructureTemplateManager templateManager)
	{
		id = idIn;
		type = typeIn;
		piece = pieceIn;
		rotation = piece.getRotation();
		bounds = piece.getBoundingBox();
		if(templateManager != null)
			calculateConnectors(templateManager);
	}
	
	public UUID id() { return id; }
	
	public NbtCompound writeToNbt(NbtCompound nbt, StructureContext context)
	{
		nbt.putUuid("ID", id);
		VillagePart.CODEC.encodeStart(NbtOps.INSTANCE, type).resultOrPartial(LOGGER::error).ifPresent(type -> nbt.put("PartType", type));
		nbt.put("Piece", piece.toNbt(context));
		if(!connectors.isEmpty())
			nbt.put("Connectors", VillageModel.connectorsToNbt(connectors));
		
		return nbt;
	}
	
	public static Optional<VillagePartInstance> readFromNbt(NbtCompound nbt, ServerWorld world)
	{
		UUID id = nbt.getUuid("ID");
		VillagePart type = VillagePart.CODEC.parse(NbtOps.INSTANCE, nbt.get("PartType")).getOrThrow();
		PoolStructurePiece piece = (PoolStructurePiece)pieceFromNbt(nbt.getCompound("Piece"), StructureContext.from(world));
		if(piece == null)
			return Optional.empty();
		
		VillagePartInstance part = new VillagePartInstance(id, type, piece, world.getStructureTemplateManager());
		part.connectors.clear();
		if(nbt.contains("Connectors", NbtElement.LIST_TYPE))
			part.connectors.addAll(VillageModel.nbtToConnectors(nbt.getList("Connectors", NbtElement.COMPOUND_TYPE)));
		return Optional.of(part);
	}
	
	@Nullable
	protected static StructurePiece pieceFromNbt(NbtCompound nbt, StructureContext context)
	{
		StructurePieceType type = Registries.STRUCTURE_PIECE.get(JIGSAW);
		if(type == null)
		{
			LOGGER.error("Unknown structure piece id: {}", JIGSAW);
			return null;
		}
		try
		{
			return type.load(context, nbt);
		}
		catch(Exception e)
		{
			LOGGER.error("Exception loading structure piece with id {}", JIGSAW, e);
		}
		return null;
	}
	
	public List<BlockPos> getAllTiles(ServerWorld world)
	{
		List<BlockPos> tileEntities = Lists.newArrayList();
		for(int y = bounds.getMinY(); y < bounds.getMaxY(); y++)
			for(int x = bounds.getMinX(); x < bounds.getMaxX(); x++)
				for(int z = bounds.getMinZ(); z < bounds.getMinZ(); z++)
				{
					BlockPos pos = new BlockPos(x, y, z);
					BlockEntity tile = world.getBlockEntity(pos);
					if(tile != null)
						tileEntities.add(pos);
				}
		return tileEntities;
	}
	
	public List<BlockPos> getTilesOfType(ServerWorld world, BlockEntityType<?> type)
	{
		List<BlockPos> tileEntities = Lists.newArrayList();
		for(int y = bounds.getMinY(); y < bounds.getMaxY(); y++)
			for(int x = bounds.getMinX(); x < bounds.getMaxX(); x++)
				for(int z = bounds.getMinZ(); z < bounds.getMaxZ(); z++)
				{
					BlockPos pos = new BlockPos(x, y, z);
					world.getBlockEntity(pos, type).ifPresent(t -> tileEntities.add(pos));
				}
		return tileEntities;
	}
	
	public void calculateConnectors(StructureTemplateManager templateManager)
	{
		connectors.clear();
		StructurePoolElement element = piece.getPoolElement();
		element.getStructureBlockInfos(templateManager, piece.getPos(), piece.getRotation(), random()).stream()
			.filter(info -> isConnector(info, bounds))
			.forEach(info -> connectors.add(new Connector(info)));
	}
	
	public static boolean isConnector(StructureBlockInfo info, BlockBox bounds)
	{
		return 
				info.state().isOf(Blocks.JIGSAW) && 
				HOVillageParts.byID(Identifier.of(info.nbt().getString(JigsawBlockEntity.TARGET_KEY))) != null && 
				!bounds.contains(info.pos().offset(JigsawBlock.getFacing(info.state())));
	}
	
	public void translate(BlockPos translation, StructureTemplateManager templateManager)
	{
		piece.translate(translation.getX(), translation.getY(), translation.getZ());
		bounds = piece.getBoundingBox();
		calculateConnectors(templateManager);
	}
	
	public Optional<BlockPos> getOffsetToLinkTo(Connector connector)
	{
		return connectors.stream().filter(c -> c.linksTo(connector)).map(c -> connector.pos.subtract(c.linkPos())).findAny();
	}
	
	public void lockConnectorAt(BlockPos position, boolean shouldNotify)
	{
		connectors.removeIf(info -> 
		{
			BlockPos pos = info.pos;
			boolean bl = pos.getSquaredDistance(position) == 0D;
			if(bl && shouldNotify)
				Hrrmowners.forAllPlayers(player -> HideCubesPacket.send(player, new DebugCuboid(pos, pos, HOVillageParts.WEAPONSMITH.get(), info.name)));
			return bl;
		});
	}
	
	public boolean hasOpenConnections() { return !connectors.isEmpty(); }
	
	public List<Connector> openConnections() { return connectors; }
	
	public void placeInWorld(ServerWorld world)
	{
		piece.generate(world, world.getStructureAccessor(), world.getChunkManager().getChunkGenerator(), random(), bounds, pivot(), false);
	}
	
	public Random random() { return Random.create(pivot().getX() * pivot().getZ() * pivot().getY()); }
	
	public BlockPos pivot() { return bounds.getCenter(); }
	
	public BlockPos min() { return new BlockPos(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ()); }
	public BlockPos max() { return new BlockPos(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ()); }
	
	public Box bounds() { return Box.enclosing(min(), max()); }
	
	public boolean contains(BlockPos pos) { return bounds.contains(pos); }
	public boolean intersects(VillagePartInstance part) { return bounds.intersects(part.bounds); }
	
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
		connectors.forEach(info -> collection.add(new DebugCuboid(info.pos, info.pos, HOVillageParts.WEAPONSMITH.get(), info.name)));
	}
}