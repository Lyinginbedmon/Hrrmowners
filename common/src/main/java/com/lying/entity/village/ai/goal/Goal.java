package com.lying.entity.village.ai.goal;

import com.lying.entity.village.VillageModel;

@FunctionalInterface
public interface Goal
{
	/* Returns the satisfaction of this goal by the given model, between 0 and 1 */
	public float satisfaction(VillageModel model);
}
