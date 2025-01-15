package com.lying.entity;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.lying.init.HOMemoryModuleTypes;
import com.lying.init.HOVillagerProfessions;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.util.math.GlobalPos;

public class HOATaskManager
{
	public static final MemoryModuleType<Boolean> RECEIVER_MODULE = HOMemoryModuleTypes.RECEIVING_TASK.get();
	public static final MemoryModuleType<GlobalPos> TASK_MODULE = HOMemoryModuleTypes.HOA_TASK.get();
	
	private final SurinaEntity entity;
	
	public HOATaskManager(SurinaEntity entityIn)
	{
		entity = entityIn;
	}
	
	public boolean canPerformHOATask() { return !hasHOATask() && isSuitableForTasks(entity); }
	
	public static boolean isSuitableForTasks(SurinaEntity entity)
	{
		return
				!entity.isBaby() &&
				!entity.isRemoved() &&
				entity.isAlive() &&
				entity.getVillagerData().getProfession() != HOVillagerProfessions.QUEEN.get() &&
				entity.hasVillage();
	}
	
	public boolean setHOATask(@Nullable GlobalPos posIn)
	{
		if(entity.getWorld().isClient())
			return false;
		
		if(posIn == null)
		{
			entity.getBrain().forget(TASK_MODULE);
			entity.getBrain().forget(RECEIVER_MODULE);
		}
		else
		{
			entity.getBrain().remember(RECEIVER_MODULE, true);
			entity.getBrain().remember(TASK_MODULE, posIn);
		}
		
		return true;
	}
	
	public boolean hasHOATask()
	{
		Optional<Boolean> mem1 = entity.getBrain().getOptionalMemory(RECEIVER_MODULE);
		if(mem1.isPresent() && mem1.get())
			return true;
		
		Optional<GlobalPos> memory = entity.getBrain().getOptionalMemory(TASK_MODULE);
		return memory.isPresent() && memory.get() != null;
	}
	
	public boolean hasHOATaskAt(GlobalPos pos)
	{
		GlobalPos target = getHOATask();
		return !hasHOATask() ? false : target.dimension() == pos.dimension() && target.pos().getSquaredDistance(pos.pos()) < 1D;
	}
	
	@Nullable
	public GlobalPos getHOATask()
	{
		return entity.getBrain().getOptionalMemory(TASK_MODULE).orElse(null);
	}
	
	public void markHOATaskCompleted()
	{
		setHOATask(null);
	}
}