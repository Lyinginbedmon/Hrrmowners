package com.lying.entity.village.ai.goal;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.lying.entity.village.VillageModel;
import com.lying.entity.village.VillagePart;
import com.lying.entity.village.VillagePartGroup;
import com.lying.entity.village.VillagePartInstance;

import net.minecraft.util.math.MathHelper;

public class GoalTypeMinimum implements Goal
{
	private final Predicate<VillagePartInstance> predicate;
	private final Function<VillageModel, Integer> count;
	
	public static GoalTypeMinimum ofType(Supplier<VillagePart> typeIn, int countIn)
	{
		return ofType(typeIn, model -> countIn);
	}
	
	public static GoalTypeMinimum ofType(Supplier<VillagePart> typeIn, Function<VillageModel, Integer> countIn)
	{
		return new GoalTypeMinimum(i -> i.type == typeIn.get(), countIn);
	}
	
	public static GoalTypeMinimum ofGroup(Supplier<VillagePartGroup> groupIn, int countIn)
	{
		return ofGroup(groupIn, model -> countIn);
	}
	
	public static GoalTypeMinimum ofGroup(Supplier<VillagePartGroup> groupIn, Function<VillageModel, Integer> countIn)
	{
		return new GoalTypeMinimum(i -> i.type.group() == groupIn.get(), countIn);
	}
	
	protected GoalTypeMinimum(Predicate<VillagePartInstance> predicateIn, Function<VillageModel, Integer> countIn)
	{
		predicate = predicateIn;
		count = countIn;
	}
	
	public float satisfaction(VillageModel model)
	{
		float count = Math.max(this.count.apply(model), 0);
		return count == 0F ? 1F: MathHelper.clamp((float)model.getTallyMatching(predicate) / count, 0F, 1F);
	}
}
