package com.lying.entity.village.ai.action;

import com.lying.entity.village.VillageModel;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public abstract class Action
{
	private final Identifier name;
	private final float cost;
	
	protected long seed = 418959963L;
	protected Random rand;
	
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
	
	/** Applies this action to the given model, returning true if successful */
	public abstract boolean applyToModel(VillageModel model, ServerWorld world, boolean isSimulated);
	
	public abstract Action copy();
}
