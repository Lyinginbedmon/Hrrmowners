package com.lying.entity.village.ai;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.lying.entity.village.VillageModel;
import com.lying.entity.village.ai.action.Action;
import com.lying.entity.village.ai.goal.Goal;
import com.mojang.datafixers.util.Pair;

import net.minecraft.server.world.ServerWorld;

public class HOAFullScan extends HOA
{
	public HOAFullScan(List<Action> actionsIn, List<Pair<Integer, Goal>> goalsIn)
	{
		super(actionsIn, goalsIn);
	}
	
	@Nullable
	protected Plan examineOptions(SearchEntry checking, Consumer<SearchEntry> addToSearch, ServerWorld world)
	{
		Plan plan = checking.plan();
		VillageModel presentState = checking.state();
		if(!testAxioms(presentState))
		{
			// If we somehow logged a model that fails our axioms, refund this iteration
			LOGGER.warn(" # # Logged village model failed axioms");
			return null;
		}
		
		// Iterate from our current best plan to identify potential next steps
		for(Action action : getOptions(presentState).stream().filter(a -> a.canAddToPlan(plan, presentState)).toList())
		{
			action.setSeed(world.random.nextLong());
			for(Action variant : action.getViablePermutations(presentState, world))
			{
				// Discard any permutation that isn't applicable for some reason or that results in the village model failing axioms
				VillageModel modelAfter = presentState.copy(world);
				if(!variant.tryApplyTo(modelAfter, world) || !testAxioms(modelAfter))
					continue;
				
				// The state of the plan after we add this action to it
				Plan planAfter = plan.copy().add(variant.copy());
				
				// If we find a plan that satisfies all objectives 100%, exit search immediately
				if(meetsObjectives(modelAfter))
					return planAfter;
				
				addToSearch.accept(new SearchEntry(modelAfter, planAfter));
			}
		}
		
		return null;
	}
}
