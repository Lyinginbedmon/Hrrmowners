package com.lying.entity.ai.brain.task;

import com.lying.entity.SurinaEntity;
import com.lying.init.HOBlockEntityTypes;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class QueenWorkTask extends SurinaWorkTask
{
	protected void performAdditionalWork(ServerWorld world, SurinaEntity entity)
	{
		// TODO Dismount non-seat vehicles
		
		if(entity.hasVehicle())
			return;
		
		Brain<SurinaEntity> brain = entity.getBrain();
		brain.getOptionalMemory(MemoryModuleType.JOB_SITE).ifPresent(memory -> 
		{
			if(entity.getWorld().getRegistryKey() != memory.dimension())
				return;
			
			BlockPos pos = memory.pos();
			world.getBlockEntity(pos, HOBlockEntityTypes.NEST.get()).ifPresent(tile -> tile.tryToSeat(entity));
		});
	}
}
