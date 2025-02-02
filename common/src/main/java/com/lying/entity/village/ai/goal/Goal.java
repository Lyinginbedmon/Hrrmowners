package com.lying.entity.village.ai.goal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.entity.village.VillageModel;
import com.lying.entity.village.ai.action.Action;

import net.minecraft.util.Identifier;

public abstract class Goal
{
	private final Identifier registryName;
	private final List<Action> controlledActions = Lists.newArrayList();
	
	protected Goal(Identifier regName, Action... actionsIn)
	{
		registryName = regName;
		addAction(actionsIn);
	}
	
	public final Identifier registryName() { return registryName; }
	
	/** 
	 * Returns the satisfaction of this goal by the given model, between 0F and 1F<br>
	 * This value is then cached in the model until it is modified.
	 */
	public final float satisfaction(VillageModel model)
	{
		// Check if this goal has already been calculated for this model before
		float score = model.getSatisfaction(registryName);
		if(score >= 0F)
			return score;
		
		// If it hasn't, calculate it and cache the result in the model
		model.cacheSatisfaction(registryName, score = evaluate(model));
		return score;
	}
	
	/** Returns a score between 0F and 1F representing this goal's satisfaction by the given model */
	protected abstract float evaluate(VillageModel model);
	
	/** Actions provided by this goal when satisfaction of it is below 100% */
	public List<Action> getActions(VillageModel model) { return satisfaction(model) < 1F ? controlledActions : List.of(); }
	
	/** Adds one or more actions to be controlled by this goal */
	public Goal addAction(Action... actionsIn)
	{
		Map<Identifier, Action> actions = new HashMap<>();
		controlledActions.forEach(a -> actions.put(a.registryName(), a));
		
		for(int i=0; i<actionsIn.length; i++)
		{
			Action action = actionsIn[i];
			if(action == null) continue;
			actions.put(action.registryName(), action);
		}
		controlledActions.addAll(actions.values());
		return this;
	}
}
