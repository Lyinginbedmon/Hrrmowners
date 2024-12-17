package com.lying.entity.village.ai;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.lying.Hrrmowners;
import com.lying.entity.village.Village;
import com.lying.entity.village.VillageModel;
import com.lying.entity.village.ai.action.Action;
import com.lying.entity.village.ai.goal.Goal;
import com.mojang.datafixers.util.Pair;

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
	
	private final List<Pair<Integer,Goal>> goals = Lists.newArrayList();
	private final Comparator<SearchEntry> stateComparator = (a, b) -> 
	{
		float satA = a.totalValue(this::goalSatisfaction);
		float satB = b.totalValue(this::goalSatisfaction);
		if(satA != satB)
			return satA > satB ? -1 : 1;
		
		int lenA = a.plan().length();
		int lenB = b.plan().length();
		return lenA < lenB ? -1 : lenA > lenB ? 1 : 0;	
	};
	
	private Predicate<VillageModel> axioms = Predicates.alwaysTrue();
	
	private List<Action> currentPlan = Lists.newArrayList();
	private Action currentAction = null;
	
	public HOA()
	{
		this(List.of(), List.of());
	}
	
	public HOA(List<Action> actionsIn, List<Pair<Integer,Goal>> goalsIn)
	{
		actionsIn.forEach(a -> addAction(a));
		goalsIn.forEach(g -> addGoal(g));
	}
	
	/**
	 * Axioms function like inviolate conditions for resulting village models.<br>
	 * The most common axiom is that the model must have at least one open connector, to allow for further construction.<br>
	 * The planner will never take actions which result in a model that fails any axiom
	 */
	public void addAxiom(Predicate<VillageModel> axiomIn)
	{
		axioms = axioms.and(axiomIn);
	}
	
	public void addAction(Action actionIn)
	{
		actions.add(actionIn);
		actions.sort(ACTION_SORTER);
	}
	
	public void addGoal(Goal goalIn)
	{
		addGoal(1, goalIn);
	}
	
	public void addGoal(int weightIn, Goal goalIn)
	{
		addGoal(Pair.of(weightIn, goalIn));
	}
	
	public void addGoal(Pair<Integer, Goal> goalIn)
	{
		goals.add(goalIn);
	}
	
	public boolean hasPlan() { return !currentPlan.isEmpty() || currentAction != null; }
	
	public void setPlan(Plan planIn)
	{
		clearPlan();
		
		// Trim unnecessary actions (usually connector inc/dec)
		// This reduces the amount of time spent on the plan during execution
		List<Action> newPlan = planIn.actions();
		while(!newPlan.isEmpty() && newPlan.getLast().shouldTrim())
			newPlan.removeLast();
		
		if(!newPlan.isEmpty())
			currentPlan.addAll(newPlan);
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
				invalidate(village);
				return;
			}
		}
		
		switch(currentAction.enactAction(village.model(), village, world))
		{
			case FAILURE:
				invalidate(village);
				break;
			case SUCCESS:
				currentAction = null;
				break;
			case RUNNING:
			default:
				break;
		}
	}
	
	protected void invalidate(Village village)
	{
		if(currentAction != null)
			LOGGER.info("## HOA plan invalidated with action {} at {} ##", currentAction.registryName().toString(), village.model().getCenter().get().pivot().toShortString());
		clearPlan();
	}
	
	public Optional<Action> currentAction() { return this.currentAction == null ? Optional.empty() : Optional.of(this.currentAction); }
	
	/** Returns true if the given model evaluates to 100% goal satisfaction */
	public boolean meetsObjectives(VillageModel model)
	{
		return goals.isEmpty() || goalSatisfaction(model) == 1F;
	}
	
	/** Returns the overal goal satisfaction of the given model */
	public float goalSatisfaction(VillageModel model)
	{
		if(goals.isEmpty())
			return 1F;
		
		float total = 0F;
		float weightSum = 0F;
		for(Pair<Integer, Goal> goal : goals)
		{
			float weight = goal.getFirst();
			total += weight * goal.getSecond().satisfaction(model);
			weightSum += weight;
		}
		return total / weightSum;
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
		LOGGER.info("#= Initial Status");
		LOGGER.info("# Goal satisfaction {}", goalSatisfaction(model));
		LOGGER.info("# Population {} ({} residents)", model.population(), model.residentPop());
		LOGGER.info("# {} available actions", actions.size());
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
		if(!axioms.test(model))
		{
			LOGGER.info(" # Current village model fails one or more axioms, cannot make plan");
			return null;
		}
		
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
			if(!axioms.test(presentState))
			{
				// If we somehow logged a model that fails our axioms, refund this iteration
				LOGGER.warn(" # # Logged village model fails axioms, refunding iteration");
				iteration--;
				continue;
			}
			
			// Iterate from our best plan to identify potential next steps
			for(Action action : actions.stream().filter(a -> a.canTakeAction(presentState) && a.canAddToPlan(plan.plan(), presentState)).toList())
			{
				action.setSeed(world.random.nextLong());
				VillageModel planModel = presentState.copy(world);
				
				// Discard any action that isn't applicable for some reason or that results in the village model failing axioms
				if(!action.consider(planModel, world) || !axioms.test(planModel))
					continue;
				
				// The state of the plan after we add this action to it
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
			return satisfaction.apply(state);
		}
	};
	
	public static class ModelStates
	{
		private Map<VillageModel, Plan> states = new HashMap<>();
		
		public boolean put(SearchEntry pair)
		{
			return put(pair.state(), pair.plan());
		}
		
		/** Adds the given plan and model to the map, returning true.<br>Returns false if an existing better plan exists for the same model state. */
		public boolean put(VillageModel model, Plan plan)
		{
			// If an existing plan results in the same model state, store only the best plan
			for(Entry<VillageModel, Plan> entry : states.entrySet())
				if(VillageModel.isEquivalent(entry.getKey(), model))
				{
					if(entry.getValue().length() < plan.length())
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
