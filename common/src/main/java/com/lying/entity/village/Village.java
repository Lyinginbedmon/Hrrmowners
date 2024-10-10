package com.lying.entity.village;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.entity.SurinaEntity;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class Village
{
	private final UUID id;
	private final RegistryKey<World> dimension;
	private final RegistryEntry<Biome> biome;
	
	/** True if this village exists in the world yet */
	private boolean inWorld = false;
	
	/** Cached array of residents within the boundaries of the village */
	private final List<SurinaEntity> residents = Lists.newArrayList();
	
	/** A set of VillagePart reflecting the layout of the village */
	private final VillageModel model = new VillageModel();
	
	public Village(UUID idIn, RegistryKey<World> dimIn, RegistryEntry<Biome> biomeIn)
	{
		id = idIn;
		dimension = dimIn;
		biome = biomeIn;
	}
	
	public UUID id() { return this.id; }
	
	public void setInWorld() { this.inWorld = true; }
	
	@SuppressWarnings("deprecation")
	public boolean isLoaded(World world)
	{
		if(!inWorld || model.getCenter().isEmpty())
			return false;
		
		VillagePart core = model.getCenter().get();
		return world.isChunkLoaded(core.min()) && world.isChunkLoaded(core.max());
	}
	
	public void tick(ServerWorld world)
	{
		// Periodically evaluate goals and update plan if necessary
	}
	
	public void erase(ServerWorld world)
	{
		model.eraseAll(world, dimension);
	}
	
	/** Adds a random new part to the village */
	public boolean grow(ServerWorld world)
	{
		Random rand = world.getRandom();
		PartType type = PartType.values()[rand.nextInt(PartType.values().length)];
		return tryAddOfType(world, type, rand);
	}
	
	public boolean tryAddOfType(ServerWorld world, PartType type, Random rand)
	{
		if(model.isEmpty() || model.cannotExpand())
		{
			Hrrmowners.LOGGER.error("Villages cannot be founded haphazardly");
			return false;
		}
		
		/**
		 * Generate a random new VillagePart
		 * Find an available connector that can link to the new part
		 * If fail, create new part with different rotation
		 * If success, add that part to the model and generate
		 */
		
		/** The village center defines the starting rotation of future parts */
		BlockRotation baseRotation = model.getCenter().get().rotation;
		BlockRotation[] rotations = BlockRotation.values();
		
		for(int i=0; i<rotations.length; i++)
		{
			BlockRotation rotation = rotations[(baseRotation.ordinal() + i)%rotations.length];
			Optional<VillagePart> partOpt = makeNewPart(BlockPos.ORIGIN, rotation, world, type, type.getStructurePool(biome), rand);
			if(partOpt.isEmpty())
			{
				Hrrmowners.LOGGER.error("Failed to create new part to expand village");
				return false;
			}
			
			VillagePart part = partOpt.get();
			for(StructureBlockInfo connector : model.connectors())
			{
				Optional<BlockPos> connectOffset = part.getOffsetToLinkTo(connector);
				if(connectOffset.isPresent())
				{
					// FIXME Ensure new part is properly translated into its correct place before generation
					part.translate(connectOffset.get(), world.getStructureTemplateManager());
					
					// Ensure part won't intersect with another part in this position
					if(model.wouldIntersect(part))
						continue;
					
					addPart(part, world, true);
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean addPart(VillagePart part, ServerWorld world, boolean generate)
	{
		boolean result = model.addPart(part, world);
		if(inWorld)
		{
			part.notifyObservers(world.getRegistryKey());
			if(generate)
				part.placeInWorld(world);
		}
		return result;
	}
	
	public void notifyObservers()
	{
		if(!inWorld) return;
		model.notifyObservers(dimension);
	}
	
	public boolean intersects(Village village)
	{
		for(VillagePart part : village.model.parts())
			if(model.wouldIntersect(part))
				return true;
		return false;
	}
	
	public boolean contains(BlockPos position) { return model.contains(position); }
	
	public void generateAll(ServerWorld world)
	{
		model.parts().forEach(part -> part.placeInWorld(world));
	}
	
	public static Optional<VillagePart> makeNewPart(final BlockPos position, final BlockRotation rotation, ServerWorld server, PartType type, RegistryKey<StructurePool> poolKey, Random rand)
	{
		DynamicRegistryManager registryManager = server.getRegistryManager();
		Registry<StructurePool> registry = registryManager.get(RegistryKeys.TEMPLATE_POOL);
		StructurePoolAliasLookup aliasLookup = StructurePoolAliasLookup.create(List.of(), position, server.getSeed());
		Optional<StructurePool> optPool = Optional.of(poolKey).flatMap(key -> registry.getOrEmpty(aliasLookup.lookup(key)));
		if(optPool.isEmpty())
		{
			Hrrmowners.LOGGER.error("Structure pool is empty "+poolKey.getValue().toString());
			return Optional.empty();
		}
		
		StructureTemplateManager manager = server.getStructureTemplateManager();
		StructurePoolElement element = optPool.get().getRandomElement(rand);
		if(element.getType() == StructurePoolElementType.EMPTY_POOL_ELEMENT)
		{
			Hrrmowners.LOGGER.error("Structure pool element is empty");
			return Optional.empty();
		}
		
		PoolStructurePiece poolStructurePiece = new PoolStructurePiece(
				manager, 
				element, 
				position, 
				element.getGroundLevelDelta(), 
				rotation, 
				element.getBoundingBox(manager, position, rotation), 
				StructureLiquidSettings.APPLY_WATERLOGGING);
		
		return Optional.of(new VillagePart(UUID.randomUUID(), type, poolStructurePiece, manager));
	}
}
