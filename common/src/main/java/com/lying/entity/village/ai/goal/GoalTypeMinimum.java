package com.lying.entity.village.ai.goal;

import java.util.function.Function;
import java.util.function.Supplier;

import com.lying.entity.village.VillagePartType;
import com.lying.entity.village.VillageModel;

import net.minecraft.util.math.MathHelper;

public class GoalTypeMinimum implements Goal
{
	private final Supplier<VillagePartType> type;
	private final Function<VillageModel, Integer> count;
	
	public GoalTypeMinimum(Supplier<VillagePartType> typeIn, int countIn)
	{
		this(typeIn, (model) -> Math.abs(countIn));
	}
	
	public GoalTypeMinimum(Supplier<VillagePartType> typeIn, Function<VillageModel, Integer> countIn)
	{
		type = typeIn;
		count = countIn;
	}
	
	public float satisfaction(VillageModel model)
	{
		float count = Math.max(this.count.apply(model), 0);
		return count == 0F ? 1F: MathHelper.clamp((float)model.getTallyOf(type.get()) / count, 0F, 1F);
	}
}
