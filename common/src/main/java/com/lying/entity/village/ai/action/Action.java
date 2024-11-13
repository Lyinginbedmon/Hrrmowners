package com.lying.entity.village.ai.action;

import com.lying.entity.SurinaEntity;
import com.lying.entity.village.Village;
import com.lying.entity.village.VillageModel;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public abstract class Action
{
	private final Identifier name;
	private final float cost;
	
	protected long seed = 418959963L;
	protected Random rand;
	
	protected Result lastResult = Result.SUCCESS;
	
	protected Action(Identifier nameIn, float costIn)
	{
		name = nameIn;
		cost = costIn;
	}
	
	public Identifier registryName() { return name; }
	
	public float cost() { return this.cost; }
	
	public Action setSeed(long seedIn)
	{
		seed = seedIn;
		rand = Random.create(seed);
		return this;
	}
	
	public void resetRand() { rand = Random.create(seed); }
	
	/** Returns whether or not this action is available to the given model */
	public abstract boolean canTakeAction(VillageModel model);
	
	/** Applies this action to the given model during planning, returning true if successful */
	public abstract boolean consider(VillageModel model, ServerWorld world);
	
	/** Applies this action to the given model during execution */
	public final Result enactAction(VillageModel model, Village village, ServerWorld world)
	{
		return (this.lastResult = enact(model, village, world));
	}
	
	protected abstract Result enact(VillageModel model, Village village, ServerWorld world);
	
	/** Changes caused by pings from residents */
	public boolean acceptPing(BlockPos target, SurinaEntity resident, VillageModel model) { return false; }
	
	public final Action copy()
	{
		Action clone = makeCopy();
		clone.setSeed(seed);
		clone.lastResult = Result.SUCCESS;
		return clone;
	}
	
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
