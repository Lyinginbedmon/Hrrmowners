package com.lying.entity.village.ai.action;

import java.util.List;

import com.lying.entity.village.VillageModel;
import com.lying.entity.village.ai.Plan;
import com.lying.reference.Reference;

import net.minecraft.server.world.ServerWorld;

public class ActionRandConnector extends Action 
{
	public ActionRandConnector(float cost)
	{
		super(Reference.ModInfo.prefix("randomise_connector"), cost);
	}
	
	public boolean shouldTrim() { return true; }
	
	public boolean canTakeAction(VillageModel model) { return !model.cannotExpand() && model.connectors().size() > 1; }
	
	public boolean canAddToPlan(Plan plan, VillageModel model)
	{
		List<Action> actions = plan.actions();
		return actions.isEmpty() || !actions.getLast().registryName().equals(registryName());
	}
	
	public boolean consider(VillageModel model, ServerWorld world)
	{
		model.selectRandomConnector(rand);
		return true;
	}
	
	protected Action makeCopy() { return new ActionRandConnector(cost); }
}
