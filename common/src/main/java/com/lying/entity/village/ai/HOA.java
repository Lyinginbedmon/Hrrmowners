package com.lying.entity.village.ai;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.entity.village.Village;
import com.lying.entity.village.VillageModel;
import com.lying.entity.village.ai.action.Action;
import com.lying.entity.village.ai.goal.Goal;

import net.minecraft.server.world.ServerWorld;

/**
 * Hrrmowners Association (tm)<br>
 * A GOAP system for generating action sequences to satisfy its given goals
 */
public class HOA
{
	private static final Logger LOGGER = Hrrmowners.LOGGER;
	private static final Comparator<Action> ACTION_SORTER = (a, b) -> a.cost() < b.cost() ? -1 : a.cost() > b.cost() ? 1 : 0;
	private static long searchStart;
	
	private final List<Action> actions = Lists.newArrayList();
	
	private final List<Goal> goals = Lists.newArrayList();
	private final Comparator<SearchEntry> stateComparator = (a, b) -> 
	{
		float satA = a.totalValue(this::goalSatisfaction);
		float satB = b.totalValue(this::goalSatisfaction);
		return satA > satB ? -1 : satA < satB ? 1 : 0;
	};
	
	private List<Action> currentPlan = Lists.newArrayList();
	
	public HOA()
	{
		this(List.of(), List.of());
	}
	
	public HOA(List<Action> actionsIn, List<Goal> goalsIn)
	{
		actionsIn.forEach(a -> addAction(a));
		goalsIn.forEach(g -> addGoal(g));
	}
	
	public void addAction(Action actionIn)
	{
		actions.add(actionIn);
		actions.sort(ACTION_SORTER);
	}
	
	public void addGoal(Goal goalIn)
	{
		goals.add(goalIn);
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
		
		Action nextStep = currentPlan.get(0);
		if(nextStep.canTakeAction(village.model()))
		{
			currentPlan.get(0).applyToModel(village.model(), world, false);
			currentPlan.remove(0);
		}
		else
			currentPlan.clear();
	}
	
	/** Returns true if the given model evaluates to 100% goal satisfaction */
	public boolean meetsObjectives(VillageModel model)
	{
		return goals.isEmpty() || goals.stream().allMatch(g -> g.satisfaction(model) == 1F);
	}
	
	/** Returns the overal goal satisfaction of the given model */
	public float goalSatisfaction(VillageModel model)
	{
		if(goals.isEmpty())
			return 1F;
		
		float total = 0F;
		for(Goal g : goals)
			total += g.satisfaction(model);
		return total / goals.size();
	}
	
	public boolean tryGeneratePlan(final VillageModel model, final Village village, ServerWorld world)
	{
		// Copy the village census into the village model, so the planner can track census-dependent goals
		model.copyCensus(village);
		
		/** No actions available, presume we cannot make any plan */
		if(actions.isEmpty())
		{
			LOGGER.info("# HOA has no actions available to it");
			setPlan(Plan.blank());
			return false;
		}
		
		/** No goals registered, assume any state is acceptable */
		if(goals.isEmpty() || meetsObjectives(model))
		{
			LOGGER.info("# HOA determined no plan necessary");
			setPlan(Plan.blank());
			return true;
		}
		
		LOGGER.info("## HOA plan generation started ##");
		searchStart = System.currentTimeMillis();
		Plan finalPlan = findSatisfyingPlan(model, world);
		LOGGER.info("## HOA plan generation ended in "+(System.currentTimeMillis() - searchStart)+"ms ##");
		
		if(finalPlan != null && !finalPlan.isBlank())
		{
			LOGGER.info("# HOA generated a plan");
			finalPlan.addToLog(LOGGER);
			setPlan(finalPlan);
			return true;
		}
		else
			LOGGER.info("# HOA failed to generate a plan");
		
		return false;
	}
	
	@Nullable
	private Plan findSatisfyingPlan(final VillageModel model, ServerWorld world)
	{
		SearchEntry initial = new SearchEntry(model.copy(world), Plan.blank());
		
		// Map of village states to the shortest & cheapest plan that reached them
		ModelStates stateMap = new ModelStates();
		stateMap.put(initial);	// Initial state should always be achievable with a blank plan
		
		// List of plans to check next
		List<SearchEntry> plansToCheck = Lists.newArrayList();
		plansToCheck.add(initial);
		
		int iteration = 0;
		while(!plansToCheck.isEmpty() && stateMap.getPlanFor(this::meetsObjectives) == null)
		{
			LOGGER.info("# Trialling "+plansToCheck.size()+" plans in iteration "+(++iteration));
			
			// The current best plan
			SearchEntry plan = plansToCheck.remove(0);
			LOGGER.info(" # Current optimum: "+goalSatisfaction(plan.state()));
			
			// List of plans generated from our current best
			List<SearchEntry> nextCheck = Lists.newArrayList();
			VillageModel presentState = plan.state();
			
			// Iterate from our best plan to identify the next best step
			for(Action action : actions)
			{
				if(!action.canTakeAction(presentState))
					continue;
				
				VillageModel planModel = presentState.copy(world);
				action.applyToModel(planModel, world, true);
				Plan planAfter = plan.plan().copy().add(action);
				
				// If we find a plan that satisfies all objectives, exit search immediately
				if(meetsObjectives(planModel))
					return planAfter;
				
				nextCheck.add(new SearchEntry(planModel, planAfter));
			}
			
			// Log each new state in the state map
			for(SearchEntry p : nextCheck)
				if(stateMap.put(p.state(), p.plan()))
					plansToCheck.add(p);
			
			// Sort plans by overall goal satisfaction to prioritise checking better plans first
			plansToCheck.sort(stateComparator);
		}
		
		return stateMap.getPlanFor(this::meetsObjectives);
	}
	
	private static record SearchEntry(VillageModel state, Plan plan)
	{
		// FIXME Prioritise entries with greater satisfaction and cheaper-on-average plans
		public float totalValue(Function<VillageModel,Float> satisfaction)
		{
			return satisfaction.apply(state) / plan.cost();
		}
	};
	
	private class ModelStates
	{
		private Map<VillageModel, Plan> states = new HashMap<>();
		
		public boolean put(SearchEntry pair)
		{
			return put(pair.state(), pair.plan());
		}
		
		/** Adds the given plan and model to the map, returning true.<br>Returns false if an existing cheaper plan exists for the same model state. */
		public boolean put(VillageModel model, Plan plan)
		{
			// If an existing plan results in the same model state, store only the best plan
			for(Entry<VillageModel, Plan> entry : states.entrySet())
				if(VillageModel.isEquivalent(entry.getKey(), model))
				{
					if(entry.getValue().cost() < plan.cost())
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
