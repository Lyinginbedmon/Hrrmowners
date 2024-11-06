package com.lying.entity.village.ai;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
	private Action currentAction = null;
	
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
	
	public boolean hasPlan() { return !currentPlan.isEmpty() || currentAction != null; }
	
	public void setPlan(Plan planIn)
	{
		clearPlan();
		currentPlan.addAll(planIn.actions());
	}
	
	public void clearPlan()
	{
		currentAction = null;
		currentPlan.clear();
	}
	
	public void tickPlan(ServerWorld world, Village village)
	{
		if(!hasPlan())
			return;
		
		if(currentAction == null)
		{
			currentAction = currentPlan.remove(0);
			
			// Before trying to execute new action, ensure it is useable
			if(!currentAction.canTakeAction(village.model()))
			{
				LOGGER.info("## HOA plan invalidated with action {} at {} ##", currentAction.registryName().toString(), village.model().getCenter().get().pivot().toShortString());
				clearPlan();
				return;
			}
		}
		
		switch(currentAction.enactAction(village.model(), village, world))
		{
			case FAILURE:
				LOGGER.info("## HOA plan invalidated with action {} at {} ##", currentAction.registryName().toString(), village.model().getCenter().get().pivot().toShortString());
				clearPlan();
				break;
			case SUCCESS:
				currentAction = null;
				break;
			case RUNNING:
			default:
				break;
		}
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
		LOGGER.info("## HOA plan generation ended in {}ms ##", System.currentTimeMillis() - searchStart);
		
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
		while(iteration < 100 && !plansToCheck.isEmpty())
		{
			// The current best plan
			SearchEntry plan = plansToCheck.remove(0);
			LOGGER.info(" # Trialling plan {} with {} alternatives, satisfaction {}", ++iteration, plansToCheck.size(), goalSatisfaction(plan.state()));
			
			// List of plans generated from our current best
			List<SearchEntry> nextCheck = Lists.newArrayList();
			VillageModel presentState = plan.state();
			
			// Iterate from our best plan to identify the next best step
			for(Action action : actions.stream().filter(a -> a.canTakeAction(presentState)).toList())
			{
				action.setSeed(world.random.nextLong());
				VillageModel planModel = presentState.copy(world);
				if(!action.consider(planModel, world))
					continue;
				
				Plan planAfter = plan.plan().copy().add(action.copy());
				
				// If we find a plan that satisfies all objectives, exit search immediately
				if(meetsObjectives(planModel))
					return planAfter;
				
				nextCheck.add(new SearchEntry(planModel, planAfter));
			}
			
			// Log each new state in the state map
			for(SearchEntry p : nextCheck)
				if(stateMap.put(p.state(), p.plan()))	// Ignore any plans that reach a previously-calculated state by a less efficient means
					plansToCheck.add(p);
			
			// Sort plans by overall goal satisfaction to prioritise checking better plans first
			plansToCheck.sort(stateComparator);
		}
		
		return stateMap.getBestFor(this::goalSatisfaction);
	}
	
	private static record SearchEntry(VillageModel state, Plan plan)
	{
		// FIXME Prioritise entries with greater satisfaction and cheaper-on-average plans
		public float totalValue(Function<VillageModel,Float> satisfaction)
		{
			return satisfaction.apply(state) / plan.cost();
		}
	};
	
	public static class ModelStates
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
		
		/** Returns an Optional containing the first plan that matches the given condition */
		public Optional<Plan> getPlanFor(Predicate<VillageModel> predicate)
		{
			if(!states.isEmpty())
				return states.entrySet().stream().filter(e -> predicate.test(e.getKey())).map(e -> e.getValue()).findFirst();
			return Optional.empty();
		}
		
		/** Returns the plan with the highest score according to the given evaluator */
		public Plan getBestFor(Function<VillageModel, Float> evaluator)
		{
			if(states.isEmpty())
				return Plan.blank();
			
			Comparator<Entry<VillageModel, Plan>> comp = (a, b) -> 
			{
				float scoreA = evaluator.apply(a.getKey());
				float scoreB = evaluator.apply(b.getKey());
				return scoreA > scoreB ? -1 : scoreA < scoreB ? 1 : 0;
			};
			return states.entrySet().stream().sorted(comp).findFirst().get().getValue();
		}
	}
}
