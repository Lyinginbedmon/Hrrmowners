package com.lying.entity.ai.brain.task;

import com.lying.entity.SurinaEntity;

import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.server.world.ServerWorld;

public class QueenSpawnBabyTask extends SingleTickTask<SurinaEntity>
{
	public boolean trigger(ServerWorld world, SurinaEntity entity, long time)
	{
		if(entity.getBreedingAge() != 0 || !entity.isSittingInNest())
			return false;
		
		SurinaEntity child = (SurinaEntity)entity.createChild(world, null);
		if(child == null)
			return false;
		
		child.copyPositionAndRotation(entity);
		world.spawnEntity(child);
		entity.setBreedingAge(world.getRandom().nextBetween(20000, 28000));
		return true;
	}
}
