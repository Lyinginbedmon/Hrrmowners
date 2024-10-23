package com.lying.entity.village.ai.goal;

import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.lying.entity.village.VillageModel;
import com.lying.entity.village.ai.Connector;

import net.minecraft.util.math.MathHelper;

public class GoalHaveConnectors implements Goal
{
	private final int count;
	private final Predicate<Connector> predicate;
	
	public GoalHaveConnectors(int countIn)
	{
		count = Math.abs(countIn);
		predicate = Predicates.alwaysTrue();
	}
	
	public GoalHaveConnectors(int countIn, Predicate<Connector> qualifierIn)
	{
		count = Math.abs(countIn);
		predicate = qualifierIn;
	}
	
	public float satisfaction(VillageModel model)
	{
		return count == 0 ? 1F: MathHelper.clamp(model.openConnectors(predicate) / count, 0F, 1F);
	}
}
