package com.lying.entity.village.ai.goal;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.lying.entity.village.VillageModel;
import com.lying.entity.village.VillagePart;
import com.lying.entity.village.VillagePartGroup;
import com.lying.entity.village.VillagePartInstance;
import com.lying.entity.village.ai.action.Action;
import com.lying.entity.village.ai.action.ActionPlacePart;
import com.lying.init.HOVillageParts;
import com.lying.reference.Reference;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

public class GoalPartMinimum extends Goal
{
	private final Predicate<VillagePartInstance> predicate;
	private final Function<VillageModel, Integer> count;
	
	public static GoalPartMinimum ofType(Supplier<VillagePart> typeIn, int countIn)
	{
		return ofType(typeIn, model -> countIn);
	}
	
	public static GoalPartMinimum ofType(Supplier<VillagePart> typeIn, Function<VillageModel, Integer> countIn, Action... actionsIn)
	{
		return new GoalPartMinimum(typeIn.get().asString(), i -> i.type.equals(typeIn.get()), countIn, actionsIn);
	}
	
	public static GoalPartMinimum ofGroup(Supplier<VillagePartGroup> groupIn, int countIn, RegistryKey<Biome> biomeIn)
	{
		return ofGroup(groupIn, model -> countIn, biomeIn);
	}
	
	public static GoalPartMinimum ofGroup(Supplier<VillagePartGroup> groupIn, Function<VillageModel, Integer> countIn, RegistryKey<Biome> biomeIn)
	{
		List<Action> actions = Lists.newArrayList();
		for(VillagePart type : HOVillageParts.values())
			if(type.group().equals(groupIn.get()))
				actions.add(new ActionPlacePart(type, biomeIn));
		return new GoalPartMinimum(groupIn.get().asString(), i -> i.type.group().equals(groupIn.get()), countIn, actions.toArray(new Action[0]));
	}
	
	protected GoalPartMinimum(String prefix, Predicate<VillagePartInstance> predicateIn, Function<VillageModel, Integer> countIn, Action... actionsIn)
	{
		super(Reference.ModInfo.prefix(prefix+"_part_minimum"), actionsIn);
		predicate = predicateIn;
		count = countIn;
	}
	
	public float evaluate(VillageModel model)
	{
		float target = Math.max(count.apply(model), 0);
		return target == 0F ? 1F: MathHelper.clamp((float)model.getTallyMatching(predicate) / target, 0F, 1F);
	}
}
