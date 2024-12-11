package com.lying.entity;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.lying.init.HOMemoryModuleTypes;
import com.lying.init.HOVillagerProfessions;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.util.math.GlobalPos;

public class HOATaskManager
{
	public static final MemoryModuleType<GlobalPos> MODULE = HOMemoryModuleTypes.HOA_TASK.get();
	
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
			entity.getBrain().forget(MODULE);
//			SurinaEntity.LOGGER.info(" * {} {} task cleared, success {}", entity.getWorld().isClient() ? "CLIENT" : "SERVER", entity.getName().getString(), hasHOATask());
		}
		else
		{
			entity.getBrain().remember(MODULE, posIn);
//			SurinaEntity.LOGGER.info(" * {} {} supervision requested at {}, success {}", entity.getWorld().isClient() ? "CLIENT" : "SERVER", entity.getName().getString(), posIn.pos().toShortString(), hasHOATask());
		}
		
		return true;
	}
	
	public boolean hasHOATask()
	{
		Optional<GlobalPos> memory = entity.getBrain().getOptionalMemory(MODULE);
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
		return entity.getBrain().getOptionalMemory(MODULE).orElse(null);
	}
	
	public void markHOATaskCompleted()
	{
		setHOATask(null);
//		SurinaEntity.LOGGER.info(" * {} {} HOA task completed, success {}", entity.getWorld().isClient() ? "CLIENT" : "SERVER", entity.getName().getString(), !hasHOATask());
	}
}