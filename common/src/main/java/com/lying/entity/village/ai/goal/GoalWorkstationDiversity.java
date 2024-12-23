package com.lying.entity.village.ai.goal;

import com.lying.entity.village.VillageModel;
import com.lying.entity.village.VillagePart;
import com.lying.init.HOVillagePartGroups;
import com.lying.init.HOVillageParts;
import com.lying.reference.Reference;

public class GoalWorkstationDiversity extends Goal
{
	public GoalWorkstationDiversity()
	{
		super(Reference.ModInfo.prefix("workstation_diversity"));
	}
	
	public float satisfaction(VillageModel model)
	{
		// Total number of workstations
		int total = model.getTallyMatching(i -> i.type.group() == HOVillagePartGroups.WORK.get());
		
		// If we only have at most 1 workstation, balance is irrelevant
		if(total < 2)
			return 1F;
		
		// Number of distinct types of workstation of which we have at least 1
		int variety = 0;
		// Greatest number of any single type of workstation
		float max = Float.MIN_VALUE;
		for(VillagePart part : HOVillageParts.ofGroup(HOVillagePartGroups.WORK.get()))
		{
			int tally = model.getTallyOf(part);
			if(tally == 0)
				continue;
			
			variety++;
			
			if(tally > max)
				max = tally;
		}
		
		// The average count of each type of workstation present
		float avg = (float)total / (float)variety;
		
		// If the average count is equal to the max count, we have 100% balance
		return avg / max;
	}
}
