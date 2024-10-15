package com.lying.entity.village.ai.goal;

import com.lying.entity.village.PartType;
import com.lying.entity.village.VillageModel;

public class GoalTypeMinimum extends Goal
{
	private final PartType type;
	private final int count;
	
	public GoalTypeMinimum(PartType typeIn, int countIn)
	{
		type = typeIn;
		count = countIn;
	}
	
	public float satisfaction(VillageModel model)
	{
		return model.getCount(type) >= count ? 1F : 0F;
	}

}
