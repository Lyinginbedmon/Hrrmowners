package com.lying.entity.village.ai.action;

import com.lying.entity.village.Village;
import com.lying.entity.village.VillageModel;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public abstract class Action
{
	private final Identifier name;
	private final float cost;
	
	protected Action(Identifier nameIn, float costIn)
	{
		name = nameIn;
		cost = costIn;
	}
	
	public Identifier registryName() { return name; }
	
	public float cost() { return this.cost; }
	
	/** Returns whether or not this action is available to the given model */
	public abstract boolean canTakeAction(VillageModel model);
	
	public abstract void applyToModel(VillageModel model, Village village, ServerWorld world, boolean isSimulated);
}
