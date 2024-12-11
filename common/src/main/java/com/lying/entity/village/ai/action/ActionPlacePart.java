package com.lying.entity.village.ai.action;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;

import com.lying.Hrrmowners;
import com.lying.entity.SurinaEntity;
import com.lying.entity.village.Village;
import com.lying.entity.village.VillageModel;
import com.lying.entity.village.VillagePartInstance;
import com.lying.entity.village.VillagePart;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.biome.Biome;

public class ActionPlacePart extends Action
{
	public static final Logger LOGGER = Hrrmowners.LOGGER;
	
	private final VillagePart type;
	private final RegistryKey<Biome> style;
	
	private final RegistryKey<StructurePool> poolKey;
	
	private Optional<VillagePartInstance> partToAdd = Optional.empty();
	private Phase phase = Phase.REQUEST;
	
	public ActionPlacePart(VillagePart typeIn, RegistryKey<Biome> styleIn)
	{
		this(typeIn, styleIn, Optional.empty());
	}
	
	public ActionPlacePart(VillagePart typeIn, RegistryKey<Biome> styleIn, Optional<VillagePartInstance> partIn)
	{
		super(Identifier.of(Reference.ModInfo.MOD_ID, "place_"+typeIn.asString()), typeIn.costToBuild());
		type = typeIn;
		style = styleIn;
		poolKey = type.getStructurePool(style);
		partToAdd = partIn;
	}
	
	protected Action makeCopy() { return new ActionPlacePart(type, style, partToAdd); }
	
	public boolean canTakeAction(VillageModel model)
	{
		return !model.cannotExpand() && model.openConnectors(c -> c.canLinkTo(type)) > 0;
	}
	
	public boolean consider(VillageModel model, ServerWorld world)
	{
		(partToAdd = getSuitablePart(model, world)).ifPresent(p -> model.addPart(p, world, false));
		return partToAdd.isPresent();
	}
	
	protected Result enact(VillageModel model, Village village, ServerWorld world)
	{
		if(partToAdd.isEmpty() || model.selectedConnector().isEmpty())
			return Result.FAILURE;
		
//		LOGGER.info("{} Updating place part action, in phase {}", world.isClient() ? "CLIENT" : "SERVER", phase.name());
		BlockPos connector = model.selectedConnector().get().pos;
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
				/**
				 * If there are no residents acting on this task, return to requesting phase
				 */
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
//		LOGGER.info(" # Action {} to {} phase", phaseIn.ordinal() < phase.ordinal() ? "regressing" : "progressing", phase.name());
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
	
	private Optional<VillagePartInstance> getSuitablePart(VillageModel model, ServerWorld world)
	{
		resetRand();
		BlockRotation[] rotations = BlockRotation.values();
		BlockRotation baseRotation = rotations[rand.nextInt(rotations.length)];
		Optional<Connector> connectOpt = model.selectedConnector();
		if(connectOpt.isEmpty())
		{
			LOGGER.error("Place part action checked after canTakeAction returned true, but no connector selected");
			return Optional.empty();
		}
		
		Connector connector = connectOpt.get();
		DynamicRegistryManager registryManager = world.getRegistryManager();
		Registry<StructurePool> registry = registryManager.get(RegistryKeys.TEMPLATE_POOL);
		StructurePoolAliasLookup aliasLookup = StructurePoolAliasLookup.create(List.of(), connector.linkPos(), world.getSeed());
		Optional<StructurePool> optPool = Optional.of(poolKey).flatMap(key -> registry.getOrEmpty(aliasLookup.lookup(key)));
		if(optPool.isEmpty())
		{
			LOGGER.error("Structure pool {} is empty", poolKey.getValue().toString());
			return Optional.empty();
		}
		
		// Examine all entries in the pool with all rotations until we find one that can mate with the selected connector
		for(StructurePoolElement option : optPool.get().getElementIndicesInRandomOrder(rand))
		{
			if(option.getType() == StructurePoolElementType.EMPTY_POOL_ELEMENT)
				continue;
			
			for(int i=0; i<rotations.length; i++)
			{
				BlockRotation rotation = rotations[(baseRotation.ordinal() + i)%rotations.length];
				Optional<VillagePartInstance> partOpt = Village.makeNewPart(option, type, connector.linkPos(), rotation, world.getStructureTemplateManager());
				VillagePartInstance part = partOpt.get();
				
				Optional<BlockPos> connectOffset = part.getOffsetToLinkTo(connector);
				if(connectOffset.isPresent())
				{
					part.translate(connectOffset.get(), world.getStructureTemplateManager());
					
					// Ensure the part won't intersect with another part in this position
					if(!model.wouldIntersect(part))
						return Optional.of(part);
				}
			}
		}
		
		return Optional.empty();
	}
	
	private static enum Phase
	{
		REQUEST,
		WAIT,
		PINGED;
	}
}
