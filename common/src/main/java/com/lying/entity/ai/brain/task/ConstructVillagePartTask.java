package com.lying.entity.ai.brain.task;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.lying.entity.SurinaEntity;
import com.lying.entity.SurinaEntity.SurinaAnimation;
import com.lying.reference.Reference;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public class ConstructVillagePartTask extends MultiTickTask<SurinaEntity>
{
	/** Duration of the build_start animation */
	private static final int START_TIME = (int)(Reference.Values.TICKS_PER_SECOND * 0.5417F);
	/* Duration of the build_end animation */
	private static final int END_TIME = (int)(Reference.Values.TICKS_PER_SECOND * 0.5417F);
	/* Total time to complete construction */
	private static final int TOTAL_TIME = Reference.Values.TICKS_PER_SECOND * 5;
	
	private final MemoryModuleType<GlobalPos> modulePos;
	private final MemoryModuleType<Boolean> moduleDone;
	private final float speed;
	private final int maxDistance;
	
	private BlockPos target;
	private State currentState = null;
	private int ticksInState = 0;
	
	public ConstructVillagePartTask(MemoryModuleType<GlobalPos> posModule, MemoryModuleType<Boolean> doneModule, float walkSpeed, int maxDist)
	{
		super(ImmutableMap.of(
				MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleState.REGISTERED,
				MemoryModuleType.PATH, MemoryModuleState.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_PRESENT), Reference.Values.TICKS_PER_SECOND * 5, 9999);
		modulePos = posModule;
		moduleDone = doneModule;
		speed = walkSpeed;
		maxDistance = maxDist;
	}
	
	protected boolean shouldRun(ServerWorld world, SurinaEntity entity)
	{
		Brain<?> brain = entity.getBrain();
		if(brain.getOptionalMemory(moduleDone).orElse(false))
			return false;
		
		Optional<GlobalPos> dest = brain.getOptionalMemory(modulePos);
		if(dest.isEmpty() || world.getRegistryKey() != dest.get().dimension() || !dest.get().pos().isWithinDistance(entity.getPos(), (double)maxDistance))
			return false;
		target = dest.get().pos();
		return true;
	}
	
	protected boolean shouldKeepRunning(ServerWorld world, SurinaEntity entity, long l)
	{
		return currentState != null;
	}
	
	protected void run(ServerWorld world, SurinaEntity entity, long time)
	{
		// Identify current state
		double dist = Math.sqrt(entity.getBlockPos().getSquaredDistance(target));
		setState(dist <= 1D ? State.BUILDING : State.MOVING);
	}
	
	protected void keepRunning(ServerWorld world, SurinaEntity entity, long time)
	{
		Brain<?> brain = entity.getBrain();
		EntityNavigation navigator = entity.getNavigation();
		++ticksInState;
		switch(currentState)
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
					return;
				}
				
				double dist = Math.sqrt(entity.getBlockPos().getSquaredDistance(target));
				if(entity.getNavigation().isFollowingPath())
					return;
				else if(dist > 2D)
					stop();
				else
					setState(State.BUILDING);
				break;
			case BUILDING:
				navigator.stop();
				if(ticksInState < START_TIME)
					entity.startAnimation(SurinaAnimation.BUILD_START);
				else if(ticksInState < (TOTAL_TIME - END_TIME))
				{
					entity.startAnimation(SurinaAnimation.BUILD_MAIN);
					
					// FIXME Replace crit particles with block dust particles
					BlockState state = world.getBlockState(target);
					for(int i=0; i<15; i++)
						world.getPlayers().forEach(p -> world.spawnParticles(p, ParticleTypes.CRIT, true, entity.getX(), entity.getY(), entity.getZ(), 1, 0, 1, 0, 0.4));
				}
				else
				{
					entity.startAnimation(SurinaAnimation.BUILD_END);
					
					brain.remember(moduleDone, true);
					if(!entity.pingVillage(target))
						entity.markHOATaskCompleted();
					setState(State.COMPLETE);
				}
				break;
			case COMPLETE:
				navigator.stop();
				if(!entity.hasHOATask() && ticksInState >= END_TIME)
					stop();
				break;
		}
	}
	
	protected void finishRunning(ServerWorld world, SurinaEntity entity, long time)
	{
		entity.startAnimation(SurinaAnimation.IDLE);
		stop();
	}
	
	private void setState(State state)
	{
		ticksInState = 0;
		currentState = state;
	}
	
	private void stop()
	{
		currentState = null;
	}
	
	private static enum State
	{
		MOVING,
		BUILDING,
		COMPLETE;
	}
}
