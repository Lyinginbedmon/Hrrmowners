package com.lying.entity.village.ai.action;

import java.util.List;
import java.util.Optional;

import com.lying.Hrrmowners;
import com.lying.entity.village.VillagePartType;
import com.lying.entity.village.Village;
import com.lying.entity.village.VillageModel;
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
import net.minecraft.world.biome.Biome;

public class ActionPlacePart extends Action
{
	private final VillagePartType type;
	private final RegistryKey<Biome> style;
	
	private final RegistryKey<StructurePool> poolKey;
	
	public ActionPlacePart(VillagePartType typeIn, RegistryKey<Biome> styleIn)
	{
		super(Identifier.of(Reference.ModInfo.MOD_ID, "place_"+typeIn.asString()), typeIn.costToBuild());
		type = typeIn;
		style = styleIn;
		poolKey = type.getStructurePool(style);
	}
	
	public Action copy() { return new ActionPlacePart(type, style).setSeed(seed); }
	
	public boolean canTakeAction(VillageModel model)
	{
		return !model.cannotExpand() && model.openConnectors(c -> c.canLinkTo(type)) > 0;
	}
	
	public boolean applyToModel(VillageModel model, ServerWorld world, boolean isSimulated)
	{
		resetRand();
		BlockRotation[] rotations = BlockRotation.values();
		BlockRotation baseRotation = rotations[rand.nextInt(rotations.length)];
		
		Connector connector = model.selectedConnector();
		DynamicRegistryManager registryManager = world.getRegistryManager();
		Registry<StructurePool> registry = registryManager.get(RegistryKeys.TEMPLATE_POOL);
		StructurePoolAliasLookup aliasLookup = StructurePoolAliasLookup.create(List.of(), connector.linkPos(), world.getSeed());
		Optional<StructurePool> optPool = Optional.of(poolKey).flatMap(key -> registry.getOrEmpty(aliasLookup.lookup(key)));
		if(optPool.isEmpty())
		{
			Hrrmowners.LOGGER.error("Structure pool {} is empty", poolKey.getValue().toString());
			return false;
		}
		
		// Examine all entries in the pool with all rotations until we find one that can mate with the selected connector
		for(StructurePoolElement option : optPool.get().getElementIndicesInRandomOrder(rand))
		{
			if(option.getType() == StructurePoolElementType.EMPTY_POOL_ELEMENT)
				continue;
			
			for(int i=0; i<rotations.length; i++)
			{
				BlockRotation rotation = rotations[(baseRotation.ordinal() + i)%rotations.length];
				Optional<VillagePart> partOpt = Village.makeNewPart(option, type, connector.linkPos(), rotation, world.getStructureTemplateManager());
				VillagePart part = partOpt.get();
				
				Optional<BlockPos> connectOffset = part.getOffsetToLinkTo(connector);
				if(connectOffset.isPresent())
				{
					part.translate(connectOffset.get(), world.getStructureTemplateManager());
					
					// Ensure the part won't intersect with another part in this position
					if(model.wouldIntersect(part))
						continue;
					
					model.addPart(part, world, !isSimulated);
					if(!isSimulated)
						part.placeInWorld(world);
					
					return true;
				}
			}
		}
		
		return false;
	}
}
