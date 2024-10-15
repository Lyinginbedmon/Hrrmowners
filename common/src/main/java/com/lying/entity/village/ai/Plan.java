package com.lying.entity.village.ai;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.entity.village.ai.action.Action;

public class Plan
{
	// The actions this plan takes
	private List<Action> actions = Lists.newArrayList();
	
	// The total cost of all actions in this plan
	private float totalCost = 0F;
	
	private Plan() { }
	
	public static Plan blank() { return new Plan(); }
	
	public boolean isBlank() { return actions.isEmpty(); }
	
	public void clear()
	{
		actions.clear();
		totalCost = 0F;
	}
	
	public Plan add(Action actionIn)
	{
		actions.add(actionIn);
		totalCost += actionIn.cost();
		return this;
	}
	
	public float cost() { return totalCost; }
	
	public int length() { return actions.size(); }
	
	public float value() { return length() * cost(); }
	
	public Plan copy()
	{
		Plan clone = blank();
		actions.forEach(a -> clone.add(a));
		return clone;
	}
	
	public List<Action> actions() { return actions; }
}
