package com.lying.entity.ai.brain.task;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.lying.entity.IVillager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

public class ProfessionTasks
{
	public static <T extends PathAwareEntity & IVillager> Task<T> createLoseJobOnSiteLoss(VillagerProfession revertTo, VillagerProfession ignore)
	{
		return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.JOB_SITE)).apply(context, jobSite -> (world, entity, time) -> 
		{
			VillagerData villagerData = entity.getVillagerData();
			if(villagerData.getProfession() != revertTo && villagerData.getProfession() != ignore && entity.getExperience() == 0 && villagerData.getLevel() <= 1)
			{
				entity.setVillagerData(entity.getVillagerData().withProfession(revertTo));
				entity.reinitializeBrain(world);
				return true;
			}
			return false;
		}));
	}
	
	public static <T extends PathAwareEntity & IVillager> Task<T> createGoToWork(VillagerProfession jobless)
	{
		return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE), context.queryMemoryOptional(MemoryModuleType.JOB_SITE)).apply(context, (potentialJobSite, jobSite) -> (world, entity, time) ->
		{
			GlobalPos globalPos = (GlobalPos)context.getValue(potentialJobSite);
			if(!globalPos.pos().isWithinDistance(entity.getPos(), 2.0) && !entity.isNatural())
				return false;
			
			potentialJobSite.forget();
			jobSite.remember(globalPos);
			world.sendEntityStatus(entity, (byte)14);
			if(entity.getVillagerData().getProfession() != jobless)
				return true;
			
			MinecraftServer minecraftServer = world.getServer();
			Optional.ofNullable(minecraftServer.getWorld(globalPos.dimension())).flatMap(jobSiteWorld -> jobSiteWorld.getPointOfInterestStorage().getType(globalPos.pos())).flatMap(poiType -> Registries.VILLAGER_PROFESSION.stream().filter(profession -> profession.heldWorkstation().test((RegistryEntry<PointOfInterestType>)poiType)).findFirst()).ifPresent(profession -> {
				entity.setVillagerData(entity.getVillagerData().withProfession(profession));
				entity.reinitializeBrain(world);
			});
			return true;
		}));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends PathAwareEntity & IVillager> Task<T> createTakeJobSite(float speed, VillagerProfession jobless)
	{
		return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE), context.queryMemoryAbsent(MemoryModuleType.JOB_SITE), context.queryMemoryValue(MemoryModuleType.MOBS), context.queryMemoryOptional(MemoryModuleType.WALK_TARGET), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET)).apply(context, (potentialJobSite, jobSite, mobs, walkTarget, lookTarget) -> (world, entity, time) -> {
			if(entity.isBaby() || entity.getVillagerData().getProfession() != jobless)
				return false;
			
			BlockPos blockPos = ((GlobalPos)context.getValue(potentialJobSite)).pos();
			Optional<RegistryEntry<PointOfInterestType>> optional = world.getPointOfInterestStorage().getType(blockPos);
			if(optional.isEmpty())
				return true;
			
			context.getValue(mobs).stream()
				.filter(mob -> mob.getType() == entity.getType() && mob != entity)
				.map(villager -> (T)villager)
				.filter(LivingEntity::isAlive)
				.filter(villager -> ProfessionTasks.canUseJobSite((RegistryEntry)optional.get(), villager, blockPos))
				.findFirst().ifPresent(villager -> 
				{
					walkTarget.forget();
					lookTarget.forget();
					potentialJobSite.forget();
					if(villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE).isEmpty())
					{
						LookTargetUtil.walkTowards(villager, blockPos, speed, 1);
						villager.getBrain().remember(MemoryModuleType.POTENTIAL_JOB_SITE, GlobalPos.create(world.getRegistryKey(), blockPos));
						DebugInfoSender.sendPointOfInterest(world, blockPos);
					}
			});
			return true;
		}));
	}

	private static <T extends PathAwareEntity & IVillager> boolean canUseJobSite(RegistryEntry<PointOfInterestType> poiType, T villager, BlockPos pos)
	{
		boolean bl = villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.POTENTIAL_JOB_SITE).isPresent();
		if(bl)
			return false;
		
		Optional<GlobalPos> optional = villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);
		VillagerProfession villagerProfession = villager.getVillagerData().getProfession();
		if(villagerProfession.heldWorkstation().test(poiType))
			return optional.isEmpty() ? ProfessionTasks.canReachJobSite(villager, pos, poiType.value()) : optional.get().pos().equals(pos);
		return false;
	}

	private static boolean canReachJobSite(PathAwareEntity entity, BlockPos pos, PointOfInterestType poiType)
	{
		Path path = entity.getNavigation().findPathTo(pos, poiType.searchDistance());
		return path != null && path.reachesTarget();
	}
	
	public static <T extends PathAwareEntity & IVillager> Task<T> createWalkTowardsJobSite(final float speed)
	{
		return new MultiTickTask<T>(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleState.VALUE_PRESENT), 1200)
				{
					protected boolean shouldRun(ServerWorld arg, T arg2)
					{
						return arg2.getBrain().getFirstPossibleNonCoreActivity().map(activity -> activity == Activity.IDLE || activity == Activity.WORK || activity == Activity.PLAY).orElse(true);
					}
					
					protected boolean shouldKeepRunning(ServerWorld arg, T arg2, long l)
					{
						return arg2.getBrain().hasMemoryModule(MemoryModuleType.POTENTIAL_JOB_SITE);
					}
					
					protected void keepRunning(ServerWorld arg, T arg2, long l)
					{
						LookTargetUtil.walkTowards(arg2, arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos(), speed, 1);
					}
					
					protected void finishRunning(ServerWorld arg, T arg2, long l)
					{
						Optional<GlobalPos> optional = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
						optional.ifPresent(pos -> 
						{
							BlockPos blockPos = pos.pos();
							ServerWorld serverWorld = arg.getServer().getWorld(pos.dimension());
							if(serverWorld == null)
								return;
							PointOfInterestStorage pointOfInterestStorage = serverWorld.getPointOfInterestStorage();
							if(pointOfInterestStorage.test(blockPos, poiType -> true))
								pointOfInterestStorage.releaseTicket(blockPos);
							
							DebugInfoSender.sendPointOfInterest(arg, blockPos);
						});
						arg2.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
					}
				};
	}
}
