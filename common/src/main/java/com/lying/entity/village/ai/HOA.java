package com.lying.entity.village.ai;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
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
import net.minecraft.util.Identifier;

/**
 * Hrrmowners Association (tm)<br>
 * A GOAP system for generating action sequences to satisfy its given goals
 */
public abstract class HOA
{
	protected static final Logger LOGGER = Hrrmowners.LOGGER;
	protected static final int ITERATION_CAP = 100;
	protected static long searchStart;
	
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
	
	/** "Evergreen" actions that are not controlled by goal satisfaction */
	private final List<Action> actions = Lists.newArrayList();
	private Predicate<VillageModel> axioms = Predicates.alwaysTrue();
	private final List<Pair<Integer,Goal>> goals = Lists.newArrayList();
	
	private List<Action> currentPlan = Lists.newArrayList();
	private Action currentAction = null;
	
	protected HOA(List<Action> actionsIn, List<Pair<Integer,Goal>> goalsIn)
	{
		actionsIn.forEach(a -> addAction(a));
		goalsIn.forEach(g -> addGoal(g));
	}
	
	/**
	 * Axioms function like inviolate conditions for resulting village models.<br>
	 * The most common axiom is that the model must have at least one open connector, to allow for further construction.<br>
	 * The planner will never take actions which result in a model that fails any axiom
	 */
	public final void addAxiom(Predicate<VillageModel> axiomIn)
	{
		axioms = axioms.and(axiomIn);
	}
	
	public final void addAction(Action actionIn)
	{
		actions.add(actionIn);
	}
	
	public final void addGoal(Goal goalIn)
	{
		addGoal(1, goalIn);
	}
	
	public final void addGoal(int weightIn, Goal goalIn)
	{
		addGoal(Pair.of(weightIn, goalIn));
	}
	
	public final void addGoal(Pair<Integer, Goal> goalIn)
	{
		goals.add(goalIn);
	}
	
	public final boolean hasPlan() { return !currentPlan.isEmpty() || currentAction != null; }
	
	public final void setPlan(Plan planIn)
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
	
	public final void clearPlan()
	{
		currentAction = null;
		currentPlan.clear();
	}
	
	public final void tickPlan(ServerWorld world, Village village)
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
	
	protected final void invalidate(Village village)
	{
		if(currentAction != null)
			LOGGER.info("## HOA plan invalidated with action {} at {} ##", currentAction.registryName().toString(), village.model().getCenter().get().pivot().toShortString());
		clearPlan();
	}
	
	public final Optional<Action> currentAction() { return this.currentAction == null ? Optional.empty() : Optional.of(this.currentAction); }
	
	/** Returns true if the given model evaluates to 100% goal satisfaction */
	public final boolean meetsObjectives(VillageModel model)
	{
		return goals.isEmpty() || goalSatisfaction(model) == 1F;
	}
	
	public final boolean testAxioms(VillageModel model) { return axioms.test(model); }
	
	/** Returns the overal goal satisfaction of the given model */
	public final float goalSatisfaction(VillageModel model)
	{
		if(goals.isEmpty())
			return 1F;
		
		float total = 0F, weightSum = 0F;
		for(Pair<Integer, Goal> goal : goals)
		{
			float weight = goal.getFirst();
			float value = goal.getSecond().satisfaction(model);
			total += weight * value;
			weightSum += weight;
		}
		return total / weightSum;
	}
	
	public final boolean tryGeneratePlan(final VillageModel model, final Village village, ServerWorld world)
	{
		// Copy the village census into the village model, so the planner can track census-dependent goals
		model.copyCensus(village);
		
		/** No actions are currently available, presume we cannot make any plan */
		Collection<Action> actions = getOptions(model);
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
	protected Plan findSatisfyingPlan(final VillageModel model, ServerWorld world)
	{
		if(!testAxioms(model))
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
		while(iteration < ITERATION_CAP && !plansToCheck.isEmpty())
		{
			if(plansToCheck.size() > 1)
				plansToCheck.sort(stateComparator);
			
			// The current best plan
			SearchEntry plan = plansToCheck.remove(0);
			LOGGER.info(" # Trialling plan {} with {} alternatives, satisfaction {}", ++iteration, plansToCheck.size(), goalSatisfaction(plan.state()));
			
			Plan result = examineOptions(plan, s -> { if(stateMap.put(s)) plansToCheck.add(s); }, world);
			if(result != null)
				return result;
		}
		
		return stateMap.getBestPlanFor(this::goalSatisfaction);
	}
	
	/** Returns a list of all actions available to the given model, based on objectives */
	protected final Collection<Action> getOptions(VillageModel model)
	{
		Map<Identifier, Action> options = new HashMap<>();
		actions.stream().filter(a -> a.canTakeAction(model)).forEach(a -> options.put(a.registryName(), a));
		goals.forEach(g -> g.getSecond().getActions(model).stream().filter(a -> a.canTakeAction(model)).forEach(a -> options.put(a.registryName(), a)));
		return options.values();
	}
	
	/** Returns a plan that meets all objectives, or null if more evaluation is needed */
	@Nullable
	protected abstract Plan examineOptions(SearchEntry checking, Consumer<SearchEntry> addToSearch, ServerWorld world);
	
	protected static record SearchEntry(VillageModel state, Plan plan)
	{
		public float totalValue(Function<VillageModel,Float> satisfaction)
		{
			return satisfaction.apply(state) / plan.length();
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
		
		/** Returns the plan with the highest score according to the given evaluator */
		public Plan getBestPlanFor(Function<VillageModel, Float> evaluator)
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
