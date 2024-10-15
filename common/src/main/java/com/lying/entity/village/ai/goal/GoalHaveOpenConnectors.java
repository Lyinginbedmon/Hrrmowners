package com.lying.entity.village.ai.goal;

import com.lying.entity.village.VillageModel;

import net.minecraft.util.math.MathHelper;

public class GoalHaveOpenConnectors extends Goal
{
	private final int count;
	
	public GoalHaveOpenConnectors(int countIn)
	{
		count = countIn;
	}
	
	public float satisfaction(VillageModel model)
	{
		return MathHelper.clamp(model.connectors().size() / count, 0F, 1F);
	}
}
