package com.lying.entity.village.ai;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.entity.village.PartType;
import com.lying.entity.village.Village;
import com.lying.entity.village.VillageModel;
import com.lying.entity.village.ai.action.Action;
import com.lying.entity.village.ai.action.ActionPlacePart;
import com.lying.entity.village.ai.goal.Goal;
import com.lying.entity.village.ai.goal.GoalHaveOpenConnectors;
import com.lying.entity.village.ai.goal.GoalTypeMinimum;
import com.mojang.datafixers.util.Pair;

import net.minecraft.server.world.ServerWorld;

/**
 * Hrrmowners Association (tm)<br>
 * A GOAP system for generating action sequences to satisfy its given goals
 */
public class HOA
{
	private static final Logger LOGGER = Hrrmowners.LOGGER;
	
	private final List<Action> actions = Lists.newArrayList();
	
	private final List<Goal> goals = Lists.newArrayList();
	
	private List<Action> currentPlan = Lists.newArrayList();
	
	public HOA()
	{
		for(PartType type : PartType.values())
			actions.add(new ActionPlacePart(type));
		
		goals.add(new GoalHaveOpenConnectors(3));
		goals.add(new GoalTypeMinimum(PartType.HOUSE, 3));
		goals.add(new GoalTypeMinimum(PartType.STREET, 1));
	}
	
	public boolean hasPlan() { return !currentPlan.isEmpty(); }
	
	public void setPlan(Plan planIn)
	{
		currentPlan.clear();
		currentPlan.addAll(planIn.actions());
	}
	
	public void tickPlan(ServerWorld world, Village village)
	{
		if(currentPlan.isEmpty())
			return;
		
		currentPlan.get(0).applyToModel(village.model(), village, world, false);
		currentPlan.remove(0);
	}
	
	public boolean meetsObjectives(VillageModel model)
	{
		return goals.isEmpty() || goals.stream().allMatch(g -> g.satisfaction(model) == 1F);
	}
	
	public boolean tryGeneratePlan(final VillageModel model, final Village village, ServerWorld world)
	{
		/** No goals registered, assume any state is acceptable */
		if(goals.isEmpty() || meetsObjectives(model))
		{
			LOGGER.info("# HOA determined no plan necessary");
			setPlan(Plan.blank());
			return true;
		}
		
		/** No actions available, presume we cannot make any plan */
		if(actions.isEmpty())
		{
			LOGGER.info("# HOA has no actions available to it");
			setPlan(Plan.blank());
			return false;
		}
		
		// Map of village states to the shortest & cheapest plan that reached them
		ModelStates stateMap = new ModelStates();
		stateMap.put(model.copy(world), Plan.blank());	// Initial state should always be achievable with a blank plan
		
		// List of plans to check next
		List<Pair<VillageModel, Plan>> plansToCheck = Lists.newArrayList();
		plansToCheck.add(Pair.of(model.copy(world), Plan.blank()));
		
		int iteration = 0;
		while(stateMap.keySet().stream().noneMatch(m -> meetsObjectives(m)) && !plansToCheck.isEmpty())
		{
			System.out.println("# Trialling "+plansToCheck.size()+" plans in iteration "+(++iteration));
			List<Pair<VillageModel, Plan>> nextCheck = Lists.newArrayList();
			for(Pair<VillageModel, Plan> plan : plansToCheck)
			{
				// Iterate from each state to find the potential moves from it
				VillageModel presentState = plan.getFirst();
				for(Action action : actions)
				{
					if(!action.canTakeAction(presentState)) continue;
					
					VillageModel planModel = presentState.copy(world);
					action.applyToModel(planModel, village, world, true);
					Plan planAfter = plan.getSecond().copy().add(action);
					
					nextCheck.add(Pair.of(planModel, planAfter));
				}
			}
			
			// Log each new state in the state map
			plansToCheck.clear();
			for(Pair<VillageModel, Plan> plan : nextCheck)
				if(stateMap.put(plan.getFirst(), plan.getSecond()))
					plansToCheck.add(plan);
		}
		
		Plan finalPlan = stateMap.getPlanFor(this::meetsObjectives);
		if(finalPlan != null && !finalPlan.isBlank())
		{
			setPlan(finalPlan);
			LOGGER.info("# HOA generated a plan");
			currentPlan.forEach(a -> LOGGER.info(" # "+a.registryName().getPath()));
			return true;
		}
		else
			LOGGER.info("# HOA failed to generate a plan");
		
		return false;
	}
	
	private class ModelStates
	{
		private Map<VillageModel, Plan> states = new HashMap<>();
		
		public Collection<VillageModel> keySet() { return states.keySet(); }
		
		public boolean put(VillageModel model, Plan plan)
		{
			// If an existing plan results in the same model state, store only the best plan
			for(Entry<VillageModel, Plan> entry : states.entrySet())
				if(entry.getKey().isEquivalent(model))
				{
					if(entry.getValue().value() < plan.value())
						return false;
					
					break;
				}
			
			states.put(model, plan);
			return true;
		}
		
		public Plan getPlanFor(Predicate<VillageModel> predicate)
		{
			for(Entry<VillageModel, Plan> entry : states.entrySet())
				if(predicate.test(entry.getKey()))
					return entry.getValue();
			return null;
		}
	}
}
