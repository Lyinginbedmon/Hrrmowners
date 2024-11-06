package com.lying.entity.ai.brain.task;

import java.util.Optional;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.lying.Hrrmowners;
import com.lying.init.HOMemoryModuleTypes;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public class ConstructVillagePartTask extends MultiTickTask<PathAwareEntity>
{
	private static final Logger LOGGER = Hrrmowners.LOGGER;
	private final MemoryModuleType<GlobalPos> module;
	private final float speed;
	private final int maxDistance;
	
	private BlockPos target;
	private State status = null;
	private int ticksInState = 0;
	
	public ConstructVillagePartTask(MemoryModuleType<GlobalPos> posModule, float walkSpeed, int maxDist)
	{
		super(ImmutableMap.of(
				MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleState.REGISTERED,
				MemoryModuleType.PATH, MemoryModuleState.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_PRESENT), 150, 250);
		module = posModule;
		speed = walkSpeed;
		maxDistance = maxDist;
	}
	
	protected boolean shouldRun(ServerWorld world, PathAwareEntity entity)
	{
		Brain<?> brain = entity.getBrain();
		Optional<GlobalPos> dest = brain.getOptionalMemory(module);
		if(dest.isEmpty() || world.getRegistryKey() != dest.get().dimension() || !dest.get().pos().isWithinDistance(entity.getPos(), (double)maxDistance))
			return false;
		target = dest.get().pos();
		return true;
	}
	
	protected boolean shouldKeepRunning(ServerWorld world, PathAwareEntity entity, long l)
	{
		return status != null;
	}
	
	protected void run(ServerWorld world, PathAwareEntity entity, long time)
	{
		// Identify current state
		double dist = Math.sqrt(entity.getBlockPos().getSquaredDistance(target));
		setStatus(dist <= 1D ? State.BUILDING : State.MOVING);
	}
	
	protected void keepRunning(ServerWorld world, PathAwareEntity entity, long time)
	{
		Brain<?> brain = entity.getBrain();
		EntityNavigation navigator = entity.getNavigation();
		++ticksInState;
		switch(status)
		{
			case MOVING:
				if(ticksInState == 1)
				{
					Path path = entity.getNavigation().findPathTo(target, 0, maxDistance);
					if(path == null)
						stop();
					
					brain.remember(MemoryModuleType.PATH, path);
					brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(target, speed, 1));
					
					navigator.startMovingAlong(path, speed);
					LOGGER.info("Moving to target position {}", target.toShortString());
					return;
				}
				
				double dist = Math.sqrt(entity.getBlockPos().getSquaredDistance(target));
				if(entity.getNavigation().isFollowingPath())
					return;
				else if(dist > 1D)
					stop();
				else
					setStatus(State.BUILDING);
				break;
			case BUILDING:
				navigator.stop();
				if(ticksInState == 1)
					;	// Start building animation
				else if(ticksInState <= 60)
				{
					world.getPlayers().forEach(p -> world.spawnParticles(p, ParticleTypes.CRIT, true, entity.getX(), entity.getY(), entity.getZ(), 1, 0, 1, 0, 0.4));
				}
				else
					setStatus(State.COMPLETE);
				break;
			case COMPLETE:
				stop();
				break;
		}
	}
	
	protected void finishRunning(ServerWorld world, PathAwareEntity entity, long time)
	{
		status = null;
		entity.getBrain().forget(HOMemoryModuleTypes.VILLAGE_TASK.get());
		LOGGER.info("Construction task finished running");
	}
	
	private void setStatus(State state)
	{
		ticksInState = 0;
		status = state;
		LOGGER.info("State updated to {}", status.name());
	}
	
	private void stop()
	{
		status = null;
		LOGGER.error("Construction task stopped");
	}
	
	private static enum State
	{
		MOVING,
		BUILDING,
		COMPLETE;
	}
}
