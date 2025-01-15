package com.lying.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.lying.entity.SurinaEntity;
import com.lying.entity.SurinaEntity.SurinaAnimation;
import com.lying.init.HOMemoryModuleTypes;
import com.lying.reference.Reference;

import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

public class ReceiveHOATask extends MultiTickTask<SurinaEntity>
{
	public ReceiveHOATask()
	{
		super(ImmutableMap.of(HOMemoryModuleTypes.RECEIVING_TASK.get(), MemoryModuleState.VALUE_PRESENT), Reference.Values.TICKS_PER_SECOND * 2);
	}
	
	protected boolean shouldRun(ServerWorld world, SurinaEntity entity)
	{
		return true;
	}
	
	protected boolean shouldKeepRunning(ServerWorld world, SurinaEntity entity, long l) { return true; }
	
	protected void run(ServerWorld world, SurinaEntity entity, long time)
	{
		entity.startAnimation(SurinaAnimation.RECEIVING_TASK);
		entity.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE);
	}
	
	protected void keepRunning(ServerWorld world, SurinaEntity entity, long time)
	{
		EntityNavigation navigator = entity.getNavigation();
		navigator.stop();
	}
	
	protected void finishRunning(ServerWorld world, SurinaEntity entity, long time)
	{
		entity.startAnimation(SurinaAnimation.IDLE);
		entity.getBrain().forget(HOMemoryModuleTypes.RECEIVING_TASK.get());
	}
}
