package com.lying.entity.village.ai.action;

import com.lying.entity.village.VillageModel;
import com.lying.reference.Reference;

import net.minecraft.server.world.ServerWorld;

public class ActionIncConnector extends Action 
{
	public ActionIncConnector()
	{
		super(Reference.ModInfo.prefix("increment_connector"), 0.3F);
	}
	
	public boolean canTakeAction(VillageModel model) { return !model.cannotExpand() && model.connectors().size() > 1; }
	
	public boolean consider(VillageModel model, ServerWorld world)
	{
		model.incSelectedConnector();
		return true;
	}
	
	protected Action makeCopy() { return new ActionIncConnector(); }
}
