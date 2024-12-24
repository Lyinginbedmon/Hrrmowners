package com.lying.entity.village.ai.action;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import com.lying.entity.SurinaEntity;
import com.lying.entity.village.Village;
import com.lying.entity.village.VillageModel;
import com.lying.entity.village.ai.Plan;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public abstract class Action
{
	public static final Comparator<Action> COST_SORT = (a1,a2) -> a1.cost() < a2.cost() ? -1 : a1.cost() > a2.cost() ? 1 : 0;
	protected static final Predicate<SurinaEntity> IS_FUNCTIONAL = s -> s.isAlive() && !s.isAiDisabled() && !s.isRemoved() && !s.isBaby();
	protected static final Predicate<SurinaEntity> CAN_PERFORM_TASK = IS_FUNCTIONAL.and(s -> s.getTaskManager().canPerformHOATask());
	
	protected final Identifier name;
	protected final float cost;
	
	protected long seed = 418959963L;
	protected Random rand;
	
	protected Result lastResult = Result.SUCCESS;
	
	protected Action(Identifier nameIn, float costIn)
	{
		name = nameIn;
		cost = Math.abs(costIn);
	}
	
	public Identifier registryName() { return name; }
	
	public float cost() { return this.cost; }
	
	public boolean shouldTrim() { return false; }
	
	public Action setSeed(long seedIn)
	{
		seed = seedIn;
		rand = Random.create(seed);
		return this;
	}
	
	public void resetRand() { rand = Random.create(seed); }
	
	/** Returns whether or not this action is available to the given model */
	public abstract boolean canTakeAction(VillageModel model);
	
	/** Returns true if this action can be added to the plan of the given model, usually to avoid spamming the same action for no reward */
	public boolean canAddToPlan(Plan plan, VillageModel model) { return true; }
	
	/** Applies this action to the given model during planning, returning true if successful */
	public boolean consider(VillageModel model, ServerWorld world) { return tryApplyTo(model, world); }
	
	/** Attempts to apply this action to the given model, usually during planning */
	public abstract boolean tryApplyTo(VillageModel model, ServerWorld world);
	
	/** Returns a list of all viable permutations of this action that can be applied to the given model */
	public List<Action> getViablePermutations(VillageModel model, ServerWorld world) { return consider(model, world) ? List.of(copy()) : List.of(); }
	
	/** Applies this action to the given model during execution */
	public final Result enactAction(VillageModel model, Village village, ServerWorld world)
	{
		return (this.lastResult = enact(model, village, world));
	}
	
	/** Applies this action to the given model during execution */
	protected Result enact(VillageModel model, Village village, ServerWorld world)
	{
		return consider(model, world) ? Result.SUCCESS : Result.FAILURE;
	}
	
	/** Changes caused by pings from residents */
	public boolean acceptPing(BlockPos target, SurinaEntity resident, VillageModel model) { return false; }
	
	public final Action copy()
	{
		Action clone = makeCopy();
		clone.setSeed(seed);
		clone.lastResult = Result.SUCCESS;
		return clone;
	}
	
	/** Creates an isolated instance of this action, usually for later execution */
	protected abstract Action makeCopy();
	
	public boolean isRunning() { return this.lastResult == Result.RUNNING; }
	
	public static enum Result implements StringIdentifiable
	{
		SUCCESS(true),
		RUNNING(false),
		FAILURE(true);
		
		private final boolean isEnd;
		
		private Result(boolean end)
		{
			isEnd = end;
		}
		
		public boolean isEndState() { return isEnd; }
		
		public String asString() { return name().toLowerCase(); }
	}
}
