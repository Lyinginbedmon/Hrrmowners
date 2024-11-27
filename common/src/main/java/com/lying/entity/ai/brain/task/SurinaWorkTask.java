package com.lying.entity.ai.brain.task;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.lying.entity.SurinaEntity;

import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.GlobalPos;

public class SurinaWorkTask extends MultiTickTask<SurinaEntity>
{
	private static final int RUN_TIME = 300;
	private static final double MAX_DISTANCE = 1.73;
	private long lastCheckedTime;
	
	public SurinaWorkTask()
	{
		super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED));
	}
	
	protected boolean shouldRun(ServerWorld serverWorld, SurinaEntity surinaEntity)
	{
		if(serverWorld.getTime() - this.lastCheckedTime < RUN_TIME)
			return false;
		
		if(serverWorld.random.nextInt(2) != 0)
			return false;
		
		this.lastCheckedTime = serverWorld.getTime();
		GlobalPos globalPos = surinaEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE).get();
		return globalPos.dimension() == serverWorld.getRegistryKey() && globalPos.pos().isWithinDistance(surinaEntity.getPos(), MAX_DISTANCE);
	}
	
	protected void run(ServerWorld serverWorld, SurinaEntity surinaEntity, long l)
	{
		Brain<SurinaEntity> brain = surinaEntity.getBrain();
		brain.remember(MemoryModuleType.LAST_WORKED_AT_POI, l);
		brain.getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE).ifPresent(pos -> brain.remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(pos.pos())));
		surinaEntity.playWorkSound();
		this.performAdditionalWork(serverWorld, surinaEntity);
		if (surinaEntity.shouldRestock())
			surinaEntity.restock();
	}
	
	protected void performAdditionalWork(ServerWorld world, SurinaEntity entity) { }
	
	protected boolean shouldKeepRunning(ServerWorld serverWorld, SurinaEntity surinaEntity, long l)
	{
		Optional<GlobalPos> optional = surinaEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);
		if (optional.isEmpty())
			return false;
		
		GlobalPos globalPos = optional.get();
		return globalPos.dimension() == serverWorld.getRegistryKey() && globalPos.pos().isWithinDistance(surinaEntity.getPos(), MAX_DISTANCE);
	}
}
