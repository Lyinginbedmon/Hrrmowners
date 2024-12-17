package com.lying.entity.village.ai.action;

import java.util.List;

import com.lying.entity.village.VillageModel;
import com.lying.entity.village.ai.Plan;
import com.lying.reference.Reference;

import net.minecraft.server.world.ServerWorld;

public class ActionIncConnector extends Action 
{
	private final boolean mode;
	
	public ActionIncConnector(boolean increment, float cost)
	{
		super(Reference.ModInfo.prefix((increment ? "in" : "de")+"crement_connector"), cost);
		mode = increment;
	}
	
	public boolean shouldTrim() { return true; }
	
	public boolean canTakeAction(VillageModel model) { return !model.cannotExpand() && model.connectors().size() > 1 && (mode || model.connectorIndex() > 0); }
	
	public boolean canAddToPlan(Plan plan, VillageModel model)
	{
		int consecutive = 0;
		
		List<Action> actions = plan.actions();
		// If plan is currently blank, we can't possibly have consecutive instances
		if(actions.isEmpty())
			return true;
		
		// If the last action added was of the same type but opposite polarity from this one, deny it
		// This prevents the planner from incrementing and decrementing pointlessly
		Action last = actions.getLast();
		if(last instanceof ActionIncConnector && ((ActionIncConnector)last).mode != mode)
			return false;
		
		// Working backwards from the current last action in the plan, count consecutive instances of this action
		int index = actions.size() - 1;
		while(index >= 0 && isSameAction(actions.get(index--)))
			consecutive++;
		
		// If the number of consecutive instances is greater than or equal to the number of connectors, deny this action
		// This prevents the planner from superfluously cycling the selected connector
		return consecutive < model.openConnectors();
	}
	
	protected boolean isSameAction(Action action)
	{
		return action.registryName().equals(registryName()) || action instanceof ActionIncConnector && ((ActionIncConnector)action).mode == mode;
	}
	
	public boolean consider(VillageModel model, ServerWorld world)
	{
		model.incSelectedConnector(mode ? 1 : -1);
		return true;
	}
	
	protected Action makeCopy() { return new ActionIncConnector(mode, cost); }
}
