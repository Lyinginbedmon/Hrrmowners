package com.lying.entity.ai;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableLong;

import com.google.common.collect.ImmutableList;
import com.lying.entity.SurinaEntity;
import com.lying.init.HOEntityTypes;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.LookAtMobTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterestStorage;

public class SurinaBrainTasks
{
	@SuppressWarnings("rawtypes")
	public static <T extends PathAwareEntity> Task<T> createGoToSecondaryPosition(MemoryModuleType<List<GlobalPos>> secondaryPositions, float speed, int completionRange, int primaryPositionActivationDistance, MemoryModuleType<GlobalPos> primaryPosition)
	{
		MutableLong mutableLong = new MutableLong(0L);
		return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(secondaryPositions), context.queryMemoryValue(primaryPosition)).apply(context, (walkTarget, secondary, primary) -> (world, entity, time) -> {
			List list = (List)context.getValue(secondary);
			GlobalPos globalPos = (GlobalPos)context.getValue(primary);
			if(list.isEmpty())
				return false;
			
			GlobalPos globalPos2 = (GlobalPos)list.get(world.getRandom().nextInt(list.size()));
			if(globalPos2 == null || world.getRegistryKey() != globalPos2.dimension() || !globalPos.pos().isWithinDistance(entity.getPos(), (double)primaryPositionActivationDistance))
				return false;
			
			if(time > mutableLong.getValue())
			{
				walkTarget.remember(new WalkTarget(globalPos2.pos(), speed, completionRange));
				mutableLong.setValue(time + 100L);
			}
			return true;
		}));
	}
	
	@SuppressWarnings("rawtypes")
	public static SingleTickTask<SurinaEntity> createWalkTowards(MemoryModuleType<GlobalPos> destination, float speed, int completionRange, int maxDistance, int maxRunTime)
	{
		return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE), context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(destination)).apply(context, (cantReachWalkTargetSince, walkTarget, destinationResult) -> (world, entity, time) -> {
			GlobalPos globalPos = (GlobalPos)context.getValue(destinationResult);
			Optional optional = context.getOptionalValue(cantReachWalkTargetSince);
			if (globalPos.dimension() != world.getRegistryKey() || optional.isPresent() && world.getTime() - (Long)optional.get() > (long)maxRunTime)
			{
				entity.releaseTicketFor(destination);
				destinationResult.forget();
				cantReachWalkTargetSince.remember(time);
			}
			else if (globalPos.pos().getManhattanDistance(entity.getBlockPos()) > maxDistance)
			{
				Vec3d vec3d = null;
				int l = 0;
				while (vec3d == null || BlockPos.ofFloored(vec3d).getManhattanDistance(entity.getBlockPos()) > maxDistance)
				{
					vec3d = NoPenaltyTargeting.findTo(entity, 15, 7, Vec3d.ofBottomCenter(globalPos.pos()), 1.5707963705062866);
					if (++l != 1000)
						continue;
					entity.releaseTicketFor(destination);
					destinationResult.forget();
					cantReachWalkTargetSince.remember(time);
					return true;
				}
				walkTarget.remember(new WalkTarget(vec3d, speed, completionRange));
			}
			else if (globalPos.pos().getManhattanDistance(entity.getBlockPos()) > completionRange)
				walkTarget.remember(new WalkTarget(globalPos.pos(), speed, completionRange));
			return true;
		}));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Pair<Integer, Task<LivingEntity>> createFreeFollowTask()
	{
		return Pair.of(5, new RandomTask(ImmutableList.of(
				Pair.of(LookAtMobTask.create(EntityType.CAT, 8.0f), 8), 
				Pair.of(LookAtMobTask.create(EntityType.VILLAGER, 8.0f), 2), 
				Pair.of(LookAtMobTask.create(EntityType.PLAYER, 8.0f), 2), 
				Pair.of(LookAtMobTask.create(SpawnGroup.CREATURE, 8.0f), 1), 
				Pair.of(LookAtMobTask.create(SpawnGroup.WATER_CREATURE, 8.0f), 1), 
				Pair.of(LookAtMobTask.create(SpawnGroup.AXOLOTLS, 8.0f), 1), 
				Pair.of(LookAtMobTask.create(SpawnGroup.UNDERGROUND_WATER_CREATURE, 8.0f), 1), 
				Pair.of(LookAtMobTask.create(SpawnGroup.WATER_AMBIENT, 8.0f), 1), 
				Pair.of(LookAtMobTask.create(SpawnGroup.MONSTER, 8.0f), 1), 
				Pair.of(new WaitTask(30, 60), 2))));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Pair<Integer, Task<LivingEntity>> createBusyFollowTask()
	{
		return Pair.of(5, new RandomTask(ImmutableList.of(
				Pair.of(LookAtMobTask.create(EntityType.VILLAGER, 8.0f), 2), 
				Pair.of(LookAtMobTask.create(HOEntityTypes.SURINA.get(), 8.0f), 2), 
				Pair.of(LookAtMobTask.create(EntityType.PLAYER, 8.0f), 2), 
				Pair.of(new WaitTask(30, 60), 8))));
	}
	
	public static <T extends PathAwareEntity> Task<T> createGoToPointOfInterestTask(float speed, int completionRange)
	{
		return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply(context, walkTarget -> (world, entity, time) -> 
		{
			if (world.isNearOccupiedPointOfInterest(entity.getBlockPos()))
				return false;
			
			PointOfInterestStorage pointOfInterestStorage = world.getPointOfInterestStorage();
			int distToPoint = pointOfInterestStorage.getDistanceFromNearestOccupied(ChunkSectionPos.from(entity.getBlockPos()));
			Vec3d destination = null;
			for (int k = 0; k < 5; ++k)
			{
				Vec3d option = FuzzyTargeting.find(entity, 15, 7, pos -> -pointOfInterestStorage.getDistanceFromNearestOccupied(ChunkSectionPos.from(pos)));
				if(option == null)
					continue;
				
				int dist = pointOfInterestStorage.getDistanceFromNearestOccupied(ChunkSectionPos.from(BlockPos.ofFloored(option)));
				if(dist < distToPoint)
				{
					destination = option;
					break;
				}
				
				if(dist != distToPoint)
					continue;
				
				destination = option;
			}
			
			if(destination != null)
				walkTarget.remember(new WalkTarget(destination, speed, completionRange));
			
			return true;
		}));
	}
}
