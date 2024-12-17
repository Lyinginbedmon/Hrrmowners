package com.lying.entity.village.ai;

import java.util.List;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.lying.entity.village.ai.action.Action;

public class Plan
{
	// The actions this plan takes, in sequential order
	private List<Action> actions = Lists.newArrayList();
	
	// The total cost of all actions in this plan
	private float totalCost = 0F;
	
	private Plan() { }
	
	public void addToLog(Logger log)
	{
		log.info("# Plan of {} actions, total cost {} #", actions.size(), totalCost);
		actions.forEach(a -> log.info(" # {}", a.registryName().toString()));
	}
	
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
	
	public int length() { return actions.size(); }
	
	public float cost() { return totalCost; }
	
	public Plan copy()
	{
		Plan clone = blank();
		actions.forEach(a -> clone.add(a));
		return clone;
	}
	
	public List<Action> actions() { return actions; }
}
