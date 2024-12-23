package com.lying.entity.village.ai.goal;

import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.lying.entity.village.VillageModel;
import com.lying.entity.village.ai.Connector;
import com.lying.reference.Reference;

import net.minecraft.util.math.MathHelper;

public class GoalOpenConnectors extends Goal
{
	private final int count;
	private final Predicate<Connector> predicate;
	
	public GoalOpenConnectors(int countIn)
	{
		this(countIn, Predicates.alwaysTrue());
	}
	
	public GoalOpenConnectors(int countIn, Predicate<Connector> qualifierIn)
	{
		super(Reference.ModInfo.prefix("open_connectors"));
		count = Math.abs(countIn);
		predicate = qualifierIn;
	}
	
	public float satisfaction(VillageModel model)
	{
		return count == 0 ? 1F: MathHelper.clamp(model.openConnectors(predicate) / count, 0F, 1F);
	}
}
