package com.lying.entity.ai;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableLong;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.lying.entity.SurinaEntity;
import com.lying.init.HOEntityTypes;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.LookAtMobTask;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

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
	
	public static Task<SurinaEntity> createGoToWork()
	{
		return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE), context.queryMemoryOptional(MemoryModuleType.JOB_SITE)).apply(context, (potentialJobSite, jobSite) -> (world, entity, time) -> {
			GlobalPos globalPos = (GlobalPos)context.getValue(potentialJobSite);
			if(!globalPos.pos().isWithinDistance(entity.getPos(), 2.0) && !entity.isNatural())
				return false;
			
			potentialJobSite.forget();
			jobSite.remember(globalPos);
			world.sendEntityStatus(entity, (byte)14);
			if(entity.getVillagerData().getProfession() != VillagerProfession.NONE)
				return true;
			
			MinecraftServer minecraftServer = world.getServer();
			Optional.ofNullable(minecraftServer.getWorld(globalPos.dimension())).flatMap(jobSiteWorld -> jobSiteWorld.getPointOfInterestStorage().getType(globalPos.pos())).flatMap(poiType -> Registries.VILLAGER_PROFESSION.stream().filter(profession -> profession.heldWorkstation().test((RegistryEntry<PointOfInterestType>)poiType)).findFirst()).ifPresent(profession -> 
			{
				entity.setVillagerData(entity.getVillagerData().withProfession(profession));
				entity.reinitializeBrain(world);
			});
			return true;
		}));
	}
	
	public static Task<SurinaEntity> createLoseJobOnSiteLoss()
	{
		return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.JOB_SITE)).apply(context, jobSite -> (world, entity, time) -> 
		{
			VillagerData villagerData = entity.getVillagerData();
			if (villagerData.getProfession() != VillagerProfession.NONE && villagerData.getProfession() != VillagerProfession.NITWIT && entity.getExperience() == 0 && villagerData.getLevel() <= 1)
			{
				entity.setVillagerData(entity.getVillagerData().withProfession(VillagerProfession.NONE));
				entity.reinitializeBrain(world);
				return true;
			}
			return false;
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
	
	public static Task<SurinaEntity> createWalkTowardsJobSite(final float speed)
	{
		return new MultiTickTask<SurinaEntity>(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleState.VALUE_PRESENT), 1200)
				{
					protected boolean shouldRun(ServerWorld arg, SurinaEntity arg2)
					{
						return arg2.getBrain().getFirstPossibleNonCoreActivity().map(activity -> activity == Activity.IDLE || activity == Activity.WORK || activity == Activity.PLAY).orElse(true);
					}
					
					protected boolean shouldKeepRunning(ServerWorld arg, SurinaEntity arg2, long l)
					{
						return arg2.getBrain().hasMemoryModule(MemoryModuleType.POTENTIAL_JOB_SITE);
					}
					
					protected void keepRunning(ServerWorld arg, SurinaEntity arg2, long l)
					{
						LookTargetUtil.walkTowards((LivingEntity)arg2, arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos(), speed, 1);
					}
					
					protected void finishRunning(ServerWorld arg, SurinaEntity arg2, long l)
					{
						Optional<GlobalPos> optional = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
						optional.ifPresent(pos -> {
							BlockPos blockPos = pos.pos();
							ServerWorld serverWorld = arg.getServer().getWorld(pos.dimension());
							if (serverWorld == null) {
								return;
							}
							PointOfInterestStorage pointOfInterestStorage = serverWorld.getPointOfInterestStorage();
							if (pointOfInterestStorage.test(blockPos, poiType -> true)) {
								pointOfInterestStorage.releaseTicket(blockPos);
							}
							DebugInfoSender.sendPointOfInterest(arg, blockPos);
						});
						arg2.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
					}
				};
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
