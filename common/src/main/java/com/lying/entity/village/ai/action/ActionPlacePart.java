package com.lying.entity.village.ai.action;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.entity.SurinaEntity;
import com.lying.entity.village.Village;
import com.lying.entity.village.VillageModel;
import com.lying.entity.village.VillagePart;
import com.lying.entity.village.VillagePartInstance;
import com.lying.entity.village.ai.Connector;
import com.lying.reference.Reference;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.biome.Biome;

public class ActionPlacePart extends Action
{
	public static final Logger LOGGER = Hrrmowners.LOGGER;
	
	private final VillagePart type;
	private final Predicate<Connector> connectorPredicate;
	private final RegistryKey<Biome> style;
	
	private final RegistryKey<StructurePool> poolKey;
	
	private Optional<VillagePartInstance> partToAdd = Optional.empty();
	private Optional<Connector> placeToAdd = Optional.empty();
	private Phase phase = Phase.REQUEST;
	
	public ActionPlacePart(VillagePart typeIn, RegistryKey<Biome> styleIn)
	{
		this(typeIn, styleIn, Optional.empty(), Optional.empty());
	}
	
	public ActionPlacePart(VillagePart typeIn, RegistryKey<Biome> styleIn, Optional<VillagePartInstance> partIn, Optional<Connector> placeIn)
	{
		super(Reference.ModInfo.prefix("place_"+typeIn.asString()), typeIn.costToBuild());
		type = typeIn;
		connectorPredicate = c -> c.canLinkToGroup(type.group());
		style = styleIn;
		poolKey = type.getStructurePool(style);
		
		partToAdd = partIn;
		placeToAdd = placeIn;
	}
	
	protected Action makeCopy() { return new ActionPlacePart(type, style, partToAdd, placeToAdd); }
	
	public boolean canTakeAction(VillageModel model)
	{
		return !model.cannotExpand() && model.firstConnectorMatching(connectorPredicate).isPresent();
	}
	
	public boolean consider(VillageModel model, ServerWorld world)
	{
		placeToAdd = model.firstConnectorMatching(connectorPredicate);
		partToAdd = getAnySuitablePart(model, world);
		return tryApplyTo(model, world);
	}
	
	public boolean tryApplyTo(VillageModel model, ServerWorld world)
	{
		return placeToAdd.isPresent() && partToAdd.isPresent() && model.addPart(partToAdd.get(), world, false);
	}
	
	public List<Action> getViablePermutations(VillageModel model, ServerWorld world)
	{
		List<Action> viable = Lists.newArrayList();
		
		Optional<Connector> place = model.firstConnectorMatching(connectorPredicate);
		if(place.isEmpty())
			return List.of();
		
		placeToAdd = place;
		
		// Examine all entries in the pool and log all rotations of them that can mate with the selected connector
		List<StructurePoolElement> options = getStructures(place, world);
		if(!options.isEmpty())
		{
			BlockRotation[] rotations = BlockRotation.values();
			BlockRotation baseRotation = rotations[rand.nextInt(rotations.length)];
			Connector connector = place.get();
//			int index = 0, count = options.size();
			for(StructurePoolElement option : options)
			{
//				LOGGER.info(" # Evaluating {} structure {} of {}", type.asString(), ++index, count);
				for(int i=0; i<rotations.length; i++)
				{
					BlockRotation rotation = rotations[(baseRotation.ordinal() + i)%rotations.length];
					validateAgainstModel(option, type, connector, rotation, model, world).ifPresent(s -> 
					{
//						LOGGER.info(" # # Permutation viable with {} rotation", rotation.asString());
						partToAdd = Optional.of(s);
						viable.add(copy());
					});
				}
			}
		}
		
		return viable;
	}
	
	private Optional<VillagePartInstance> getAnySuitablePart(VillageModel model, ServerWorld world)
	{
		Optional<Connector> place = model.firstConnectorMatching(connectorPredicate);
		
		// Examine all entries in the pool with all rotations until we find one that can mate with the selected connector
		List<StructurePoolElement> options = getStructures(place, world);
		if(options.isEmpty())
			return Optional.empty();
		
		BlockRotation[] rotations = BlockRotation.values();
		BlockRotation baseRotation = rotations[rand.nextInt(rotations.length)];
		
		Connector connector = place.get();
//		int index = 0, count = options.size();
		for(StructurePoolElement option : options)
		{
//			LOGGER.info(" # Evaluating {} structure {} of {}", type.asString(), ++index, count);
			for(int i=0; i<rotations.length; i++)
			{
				BlockRotation rotation = rotations[(baseRotation.ordinal() + i)%rotations.length];
				Optional<VillagePartInstance> validated = validateAgainstModel(option, type, connector, rotation, model, world);
				if(validated.isPresent())
					return validated;
			}
		}
		
		return Optional.empty();
	}
	
	private static Optional<VillagePartInstance> validateAgainstModel(StructurePoolElement structure, VillagePart type, Connector connector, BlockRotation rotation, VillageModel model, ServerWorld world)
	{
		Optional<VillagePartInstance> partOpt = Village.makeNewPart(structure, type, connector.linkPos(), rotation, world.getStructureTemplateManager());
		if(partOpt.isEmpty())
			return Optional.empty();
		
		// Find a connector in the part that can mate to the given connector, and apply the necessary translation
		VillagePartInstance part = partOpt.get();
		Optional<BlockPos> connectOffset = part.getOffsetToLinkTo(connector);
		if(connectOffset.isPresent())
		{
			part.translate(connectOffset.get(), world.getStructureTemplateManager());
			
			// Ensure the part won't intersect with another part in this position
			if(!model.wouldIntersect(part))
				return Optional.of(part);
		}
		return Optional.empty();
	}
	
	private List<StructurePoolElement> getStructures(Optional<Connector> connectOpt, ServerWorld world)
	{
		resetRand();
		if(connectOpt.isEmpty())
		{
			LOGGER.error("Place part action checked after canTakeAction returned true, but no connector available");
			return List.of();
		}
		
		Connector connector = connectOpt.get();
		if(!connector.canLinkToGroup(type.group()))
		{
			LOGGER.error("Connector for {} cannot link to part of type {}", connector.partGroup().asString(), type.asString());
			return List.of();
		}
		
		DynamicRegistryManager registryManager = world.getRegistryManager();
		Registry<StructurePool> registry = registryManager.get(RegistryKeys.TEMPLATE_POOL);
		StructurePoolAliasLookup aliasLookup = StructurePoolAliasLookup.create(List.of(), connector.linkPos(), world.getSeed());
		Optional<StructurePool> optPool = Optional.of(poolKey).flatMap(key -> registry.getOrEmpty(aliasLookup.lookup(key)));
		if(optPool.isEmpty())
		{
			LOGGER.error("Structure pool {} is empty", poolKey.getValue().toString());
			return List.of();
		}
		
		return optPool.get().getElementIndicesInRandomOrder(rand).stream().filter(s -> s.getType() != StructurePoolElementType.EMPTY_POOL_ELEMENT).toList();
	}
	
	protected Result enact(VillageModel model, Village village, ServerWorld world)
	{
		if(partToAdd.isEmpty() || placeToAdd.isEmpty())
			return Result.FAILURE;
		
		BlockPos connector = placeToAdd.get().pos;
		GlobalPos dest = new GlobalPos(world.getRegistryKey(), connector);
		switch(phase)
		{
			case REQUEST:
				// Find an available resident to supervise construction
				village.getResidentsMatching(CAN_PERFORM_TASK).stream().sorted((a,b) -> 
				{
					// Sort closest to destination point
					double aDis = a.squaredDistanceTo(connector.getX() + 0.5D, connector.getY() + 0.5D, connector.getZ() + 0.5D);
					double bDis = b.squaredDistanceTo(connector.getX() + 0.5D, connector.getY() + 0.5D, connector.getZ() + 0.5D);
					return aDis < bDis ? -1 : aDis > bDis ? 1 : 0;
				}).findFirst().ifPresent(r -> 
				{
					if(r.getTaskManager().setHOATask(dest))
						setPhase(Phase.WAIT);
				});
				return Result.RUNNING;
			case WAIT:
				// If there are no residents acting on this task, return to requesting phase to find someone else
				if(village.getResidentsMatching(hasThisTask(dest)).isEmpty())
					setPhase(Phase.REQUEST);
				
				return Result.RUNNING;
			case PINGED:
				return Result.SUCCESS;
			default:
				phase = Phase.REQUEST;
				return Result.RUNNING;
		}
	}
	
	/** Returns true if a given SurinaEntity is functional and has an HOA task matching this position */
	private static Predicate<SurinaEntity> hasThisTask(GlobalPos pos)
	{
		return IS_FUNCTIONAL.and(r -> r.getTaskManager().hasHOATaskAt(pos));
	}
	
	private void setPhase(Phase phaseIn)
	{
		phase = phaseIn;
	}
	
	public boolean acceptPing(BlockPos target, SurinaEntity resident, VillageModel model)
	{
		if(phase != Phase.WAIT)
			return false;
		
		resident.getTaskManager().markHOATaskCompleted();
		model.addPart(partToAdd.get(), (ServerWorld)resident.getWorld(), true);
		partToAdd.get().placeInWorld((ServerWorld)resident.getWorld());
		setPhase(Phase.PINGED);
		return true;
	}
	
	private static enum Phase
	{
		REQUEST,
		WAIT,
		PINGED;
	}
}
