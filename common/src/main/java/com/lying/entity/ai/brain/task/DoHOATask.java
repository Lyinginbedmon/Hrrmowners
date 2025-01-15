package com.lying.entity.ai.brain.task;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.lying.entity.SurinaEntity;
import com.lying.entity.SurinaEntity.SurinaAnimation;
import com.lying.init.HOMemoryModuleTypes;
import com.lying.reference.Reference;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public class DoHOATask extends MultiTickTask<SurinaEntity>
{
	/** Duration of the build_start animation */
	private static final int START_TIME = (int)(Reference.Values.TICKS_PER_SECOND * 0.5417F);
	/* Duration of the build_end animation */
	private static final int END_TIME = (int)(Reference.Values.TICKS_PER_SECOND * 0.5417F);
	/* Total time to complete construction */
	private static final int TOTAL_TIME = Reference.Values.TICKS_PER_SECOND * 5;
	
	private final int maxDistance;
	private Phase prevPhase;
	private int ticksRunning = 0;
	private BlockPos target;
	
	public DoHOATask(int maxDist)
	{
		super(ImmutableMap.of(HOMemoryModuleTypes.HOA_TASK.get(), MemoryModuleState.VALUE_PRESENT, HOMemoryModuleTypes.RECEIVING_TASK.get(), MemoryModuleState.VALUE_ABSENT), TOTAL_TIME);
		maxDistance = maxDist;
	}
	
	protected boolean shouldRun(ServerWorld world, SurinaEntity entity)
	{
		GlobalPos dest = entity.getTaskManager().getHOATask();
		if(world.getRegistryKey() != dest.dimension() || !(target = dest.pos()).isWithinDistance(entity.getPos(), (double)maxDistance))
			return false;
		
		return true;
	}
	
	protected boolean shouldKeepRunning(ServerWorld world, SurinaEntity entity, long l) { return true; }
	
	protected void run(ServerWorld world, SurinaEntity entity, long time)
	{
		entity.startAnimation(SurinaAnimation.BUILD_START);
		prevPhase = null;
		ticksRunning = 0;
	}
	
	protected void keepRunning(ServerWorld world, SurinaEntity entity, long time)
	{
		EntityNavigation navigator = entity.getNavigation();
		navigator.stop();
		
		entity.getLookControl().lookAt(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);
		
		Phase phase = Phase.fromTicks(++ticksRunning);
		switch(phase)
		{
			case START:
				entity.startAnimation(SurinaAnimation.BUILD_START);
				break;
			case MAIN:
				entity.startAnimation(SurinaAnimation.BUILD_MAIN);
				// FIXME Replace crit particles with block dust particles
				BlockState state = world.getBlockState(target);
				for(int i=0; i<15; i++)
					world.getPlayers().forEach(p -> world.spawnParticles(p, ParticleTypes.CRIT, true, entity.getX(), entity.getY(), entity.getZ(), 1, 0, 1, 0, 0.4));
				break;
			case END:
				entity.startAnimation(SurinaAnimation.BUILD_END);
				if(prevPhase != Phase.END)
					entity.pingVillage(target);
				break;
		}
		prevPhase = phase;
	}
	
	protected void finishRunning(ServerWorld world, SurinaEntity entity, long time) { entity.startAnimation(SurinaAnimation.IDLE); }
	
	private static enum Phase
	{
		START(-9999, START_TIME),
		MAIN(START_TIME, TOTAL_TIME - END_TIME),
		END(END_TIME, 9999);
		
		private final int start, end;
		
		private Phase(int startIn, int endIn)
		{
			start = startIn;
			end = endIn;
		}
		
		public static Phase fromTicks(int time)
		{
			for(Phase phase : values())
				if(time >= phase.start && time < phase.end)
					return phase;
			return END;
		}
	}
	
	// Make the mob go to the position it needs to do something at
	public static Task<SurinaEntity> createGoToHOATask()
	{
		return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(HOMemoryModuleTypes.HOA_TASK.get()), context.queryMemoryOptional(MemoryModuleType.WALK_TARGET)).apply(context, (task, walk) -> (world, entity, time) -> {
			if(!entity.getTaskManager().hasHOATask())
				return false;
			
			GlobalPos pos = context.getValue(task);
			if(entity.getBlockPos().isWithinDistance(pos.pos(), 2D))
				return false;
			
			walk.remember(Optional.of(new WalkTarget(pos.pos(), 0.5F, 1)));
			return true;
		}));
	}
}
