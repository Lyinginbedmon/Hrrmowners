package com.lying.entity.village.ai.goal;

import com.lying.entity.village.VillageModel;

import net.minecraft.util.math.MathHelper;

public class GoalHaveOpenConnectors implements Goal
{
	private final int count;
	
	public GoalHaveOpenConnectors(int countIn)
	{
		count = Math.abs(countIn);
	}
	
	public float satisfaction(VillageModel model)
	{
		return count == 0 ? 1F: MathHelper.clamp(model.openConnectors() / count, 0F, 1F);
	}
}
