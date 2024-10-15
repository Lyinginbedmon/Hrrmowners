package com.lying.entity.village.ai.action;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.entity.village.PartType;
import com.lying.entity.village.Village;
import com.lying.entity.village.VillageModel;
import com.lying.entity.village.VillagePart;
import com.lying.entity.village.ai.Connector;
import com.lying.reference.Reference;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ActionPlacePart extends Action
{
	final PartType type;
	
	public ActionPlacePart(PartType typeIn)
	{
		super(Identifier.of(Reference.ModInfo.MOD_ID, "place_"+typeIn.asString()), 1F);
		type = typeIn;
	}
	
	public boolean canTakeAction(VillageModel model)
	{
		return !model.cannotExpand();
	}
	
	public void applyToModel(VillageModel model, Village village, ServerWorld world, boolean isSimulated)
	{
		BlockRotation[] rotations = BlockRotation.values();
		BlockRotation baseRotation = rotations[world.getRandom().nextInt(rotations.length)];
		
		final List<Connector> connectors = Lists.newArrayList();
		connectors.addAll(model.connectors());
		
		for(int i=0; i<rotations.length; i++)
		{
			BlockRotation rotation = rotations[(baseRotation.ordinal() + i)%rotations.length];
			Optional<VillagePart> partOpt = Village.makeNewPart(BlockPos.ORIGIN, rotation, world, type, type.getStructurePool(village.biome()), world.random);
			if(partOpt.isEmpty())
			{
				Hrrmowners.LOGGER.error("Failed to create new part to expand village");
				continue;
			}
			
			VillagePart part = partOpt.get();
			for(Connector connector : connectors)
			{
				Optional<BlockPos> connectOffset = part.getOffsetToLinkTo(connector);
				if(connectOffset.isPresent())
				{
					part.translate(connectOffset.get(), world.getStructureTemplateManager());
					
					// Ensure part won't intersect with another part in this position
					if(model.wouldIntersect(part))
						continue;
					
					model.addPart(part, world, !isSimulated);
					if(!isSimulated)
						part.placeInWorld(world);
					
					return;
				}
			}
		}
	}
}
