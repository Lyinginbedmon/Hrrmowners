package com.lying.entity.ai;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.lying.entity.SurinaEntity;
import com.lying.entity.ai.brain.task.ConstructVillagePartTask;
import com.lying.entity.ai.brain.task.ProfessionTasks;
import com.lying.entity.ai.brain.task.QueenWorkTask;
import com.lying.entity.ai.brain.task.SurinaWorkTask;
import com.lying.init.HOEntityTypes;
import com.lying.init.HOMemoryModuleTypes;
import com.lying.init.HOVillagerProfessions;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.FindEntityTask;
import net.minecraft.entity.ai.brain.task.FindInteractionTargetTask;
import net.minecraft.entity.ai.brain.task.FindPointOfInterestTask;
import net.minecraft.entity.ai.brain.task.FindWalkTargetTask;
import net.minecraft.entity.ai.brain.task.ForgetBellRingTask;
import net.minecraft.entity.ai.brain.task.ForgetCompletedPointOfInterestTask;
import net.minecraft.entity.ai.brain.task.GoToIfNearbyTask;
import net.minecraft.entity.ai.brain.task.GoToNearbyPositionTask;
import net.minecraft.entity.ai.brain.task.GoToRememberedPositionTask;
import net.minecraft.entity.ai.brain.task.GoTowardsLookTargetTask;
import net.minecraft.entity.ai.brain.task.HideInHomeTask;
import net.minecraft.entity.ai.brain.task.HideWhenBellRingsTask;
import net.minecraft.entity.ai.brain.task.JumpInBedTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.MeetVillagerTask;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.entity.ai.brain.task.PlayWithVillagerBabiesTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.ScheduleActivityTask;
import net.minecraft.entity.ai.brain.task.SleepTask;
import net.minecraft.entity.ai.brain.task.StartRaidTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.ai.brain.task.StopPanickingTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.Tasks;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WakeUpTask;
import net.minecraft.entity.ai.brain.task.WalkHomeTask;
import net.minecraft.entity.ai.brain.task.WalkToNearestVisibleWantedItemTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.ai.brain.task.WanderIndoorsTask;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestTypes;

public class SurinaTaskListProvider
{
	private static final float JOB_WALKING_SPEED = 0.4f;
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super SurinaEntity>>> createCoreTasks(VillagerProfession profession, float speed)
	{
		/** 
		 * ImmutableList.of(
			 * Pair.of(0, new StayAboveWaterTask(0.8f)), 
			 * Pair.of(0, OpenDoorsTask.create()), 
			 * Pair.of(0, new LookAroundTask(45, 90)), 
			 * Pair.of(0, new PanicTask()), 
			 * Pair.of(0, WakeUpTask.create()), 
			 * Pair.of(0, HideWhenBellRingsTask.create()), 
			 * Pair.of(0, StartRaidTask.create()), 
			 * Pair.of(0, ForgetCompletedPointOfInterestTask.create(profession.heldWorkstation(), MemoryModuleType.JOB_SITE)), 
			 * Pair.of(0, ForgetCompletedPointOfInterestTask.create(profession.acquirableWorkstation(), MemoryModuleType.POTENTIAL_JOB_SITE)), 
			 * Pair.of(1, new WanderAroundTask()), 
			 * Pair.of(2, WorkStationCompetitionTask.create()), 
			 * Pair.of(3, new FollowCustomerTask(speed)), 
			 * new Pair[]{
				 * Pair.of(5, WalkToNearestVisibleWantedItemTask.create(speed, false, 4)), 
				 * Pair.of(6, FindPointOfInterestTask.create(profession.acquirableWorkstation(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())), 
				 * Pair.of(7, new WalkTowardJobSiteTask(speed)), 
				 * Pair.of(8, TakeJobSiteTask.create(speed)), 
				 * Pair.of(10, FindPointOfInterestTask.create(poiType -> poiType.matchesKey(PointOfInterestTypes.HOME), MemoryModuleType.HOME, false, Optional.of((byte)14))), 
				 * Pair.of(10, FindPointOfInterestTask.create(poiType -> poiType.matchesKey(PointOfInterestTypes.MEETING), MemoryModuleType.MEETING_POINT, true, Optional.of((byte)14))), 
				 * Pair.of(10, GoToWorkTask.create()), 
				 * Pair.of(10, LoseJobOnSiteLossTask.create())});
		 */
		
		List<Pair<Integer, ? extends Task<? super SurinaEntity>>> tasks = Lists.newArrayList();
		tasks.add(Pair.of(0, new StayAboveWaterTask(0.8f)));
		tasks.add(Pair.of(0, OpenDoorsTask.create()));
		tasks.add(Pair.of(0, new LookAroundTask(45, 90)));
		tasks.add(Pair.of(0, WakeUpTask.create()));
		tasks.add(Pair.of(0, HideWhenBellRingsTask.create()));
		tasks.add(Pair.of(0, StartRaidTask.create()));
		tasks.add(Pair.of(0, ForgetCompletedPointOfInterestTask.create(profession.heldWorkstation(), MemoryModuleType.JOB_SITE)));
		tasks.add(Pair.of(0, ForgetCompletedPointOfInterestTask.create(profession.acquirableWorkstation(), MemoryModuleType.POTENTIAL_JOB_SITE)));
		tasks.add(Pair.of(0, new ConstructVillagePartTask(HOMemoryModuleTypes.HOA_TASK.get(), HOMemoryModuleTypes.HOA_TASK_DONE.get(), 1.0F, 64)));
		tasks.add(Pair.of(1, new WanderAroundTask()));
		
		tasks.add(Pair.of(5, WalkToNearestVisibleWantedItemTask.create(speed, false, 4)));
		tasks.add(Pair.of(6, FindPointOfInterestTask.create(profession.acquirableWorkstation(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())));
		tasks.add(Pair.of(7, ProfessionTasks.createWalkTowardsJobSite(speed)));
		tasks.add(Pair.of(8, ProfessionTasks.createTakeJobSite(speed, HOVillagerProfessions.NEET.get())));
		tasks.add(Pair.of(10, FindPointOfInterestTask.create(poiType -> poiType.matchesKey(PointOfInterestTypes.HOME), MemoryModuleType.HOME, false, Optional.of((byte)14))));
		tasks.add(Pair.of(10, FindPointOfInterestTask.create(poiType -> poiType.matchesKey(PointOfInterestTypes.MEETING), MemoryModuleType.MEETING_POINT, true, Optional.of((byte)14))));
		tasks.add(Pair.of(10, ProfessionTasks.createGoToWork(HOVillagerProfessions.NEET.get())));
		tasks.add(Pair.of(10, ProfessionTasks.createLoseJobOnSiteLoss(HOVillagerProfessions.NEET.get(), VillagerProfession.NITWIT)));
		
		return ImmutableList.copyOf(tasks);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ImmutableList<Pair<Integer, ? extends Task<? super SurinaEntity>>> createWorkTasks(VillagerProfession profession, float speed)
	{
		/*
		 * ImmutableList.of(
			 * VillagerTaskListProvider.createBusyFollowTask(), 
			 * Pair.of(5, new RandomTask(ImmutableList.of(
			 	* Pair.of(villagerWorkTask, 7), 
			 	* Pair.of(GoToIfNearbyTask.create(MemoryModuleType.JOB_SITE, 0.4f, 4), 2), 
			 	* Pair.of(GoToNearbyPositionTask.create(MemoryModuleType.JOB_SITE, 0.4f, 1, 10), 5), 
			 	* Pair.of(GoToSecondaryPositionTask.create(MemoryModuleType.SECONDARY_JOB_SITE, speed, 1, 6, MemoryModuleType.JOB_SITE), 5), 
			 	* Pair.of(new FarmerVillagerTask(), profession == VillagerProfession.FARMER ? 2 : 5), 
			 	* Pair.of(new BoneMealTask(), profession == VillagerProfession.FARMER ? 4 : 7)))), 
		 	 * Pair.of(10, new HoldTradeOffersTask(400, 1600)), 
		 	 * Pair.of(10, FindInteractionTargetTask.create(EntityType.PLAYER, 4)), 
		 	 * Pair.of(2, VillagerWalkTowardsTask.create(MemoryModuleType.JOB_SITE, speed, 9, 100, 1200)), 
		 	 * Pair.of(3, new GiveGiftsToHeroTask(100)), 
		 	 * Pair.of(99, ScheduleActivityTask.create()));
		 */
		SurinaWorkTask villagerWorkTask = new SurinaWorkTask();
		if(profession == HOVillagerProfessions.QUEEN.get())
			villagerWorkTask = new QueenWorkTask();
		if(profession == VillagerProfession.FARMER)
//			villagerWorkTask = new FarmerWorkTask()
			;
		
		return ImmutableList.of(
			SurinaBrainTasks.createBusyFollowTask(),
			Pair.of(5, new RandomTask(ImmutableList.of(
				Pair.of(villagerWorkTask, 7),
				Pair.of(GoToIfNearbyTask.create(MemoryModuleType.JOB_SITE, JOB_WALKING_SPEED, 4), 2),
				Pair.of(GoToNearbyPositionTask.create(MemoryModuleType.JOB_SITE, JOB_WALKING_SPEED, 1, 10), 5),
				Pair.of(SurinaBrainTasks.createGoToSecondaryPosition(MemoryModuleType.SECONDARY_JOB_SITE, speed, 1, 6, MemoryModuleType.JOB_SITE), 5)
				))),
			Pair.of(10, FindInteractionTargetTask.create(EntityType.PLAYER, 4)),
			Pair.of(2, SurinaBrainTasks.createWalkTowards(MemoryModuleType.JOB_SITE, speed, 9, 100, 1200)),
			Pair.of(99, ScheduleActivityTask.create()));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ImmutableList<Pair<Integer, ? extends Task<? super SurinaEntity>>> createPlayTasks(float speed)
	{
		/*
		 * ImmutableList.of(
			 * Pair.of(0, new WanderAroundTask(80, 120)), 
			 * VillagerTaskListProvider.createFreeFollowTask(), 
			 * Pair.of(5, PlayWithVillagerBabiesTask.create()), 
			 * Pair.of(5, new RandomTask(ImmutableMap.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleState.VALUE_ABSENT), ImmutableList.of(
				 * Pair.of(FindEntityTask.create(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 2), 
				 * Pair.of(FindEntityTask.create(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 1), 
				 * Pair.of(FindWalkTargetTask.create(speed), 1), 
				 * Pair.of(GoTowardsLookTargetTask.create(speed, 2), 1), 
				 * Pair.of(new JumpInBedTask(speed), 2), 
				 * Pair.of(new WaitTask(20, 40), 2)))), 
			 * Pair.of(99, ScheduleActivityTask.create()));
		 */
		return ImmutableList.of(
				Pair.of(0, new WanderAroundTask(80, 120)), 
				SurinaBrainTasks.createFreeFollowTask(), 
				SurinaBrainTasks.createFreeFollowTask(),
				Pair.of(5, PlayWithVillagerBabiesTask.create()), 
				Pair.of(5, new RandomTask(ImmutableMap.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleState.VALUE_ABSENT), ImmutableList.of(
					Pair.of(FindEntityTask.create(HOEntityTypes.SURINA.get(), 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 2), 
					Pair.of(FindEntityTask.create(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 1), 
					Pair.of(FindWalkTargetTask.create(speed), 1), 
					Pair.of(GoTowardsLookTargetTask.create(speed, 2), 1), 
					Pair.of(new JumpInBedTask(speed), 2), 
					Pair.of(new WaitTask(20, 40), 2)))), 
				Pair.of(99, ScheduleActivityTask.create()));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ImmutableList<Pair<Integer, ? extends Task<? super SurinaEntity>>> createRestTasks(VillagerProfession profession, float speed)
	{
		/*
		 * ImmutableList.of(
			 * Pair.of(2, VillagerWalkTowardsTask.create(MemoryModuleType.HOME, speed, 1, 150, 1200)), 
			 * Pair.of(3, ForgetCompletedPointOfInterestTask.create(poiType -> poiType.matchesKey(PointOfInterestTypes.HOME), MemoryModuleType.HOME)), 
			 * Pair.of(3, new SleepTask()), 
			 * Pair.of(5, new RandomTask(ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleState.VALUE_ABSENT), ImmutableList.of(
				 * Pair.of(WalkHomeTask.create(speed), 1), 
				 * Pair.of(WanderIndoorsTask.create(speed), 4), 
				 * Pair.of(GoToPointOfInterestTask.create(speed, 4), 2), 
				 * Pair.of(new WaitTask(20, 40), 2)))), 
			 * VillagerTaskListProvider.createBusyFollowTask(), 
			 * Pair.of(99, ScheduleActivityTask.create()));
		 */
		return ImmutableList.of(
				Pair.of(2, SurinaBrainTasks.createWalkTowards(MemoryModuleType.HOME, speed, 1, 150, 1200)),
				Pair.of(3, ForgetCompletedPointOfInterestTask.create(poiType -> poiType.matchesKey(PointOfInterestTypes.HOME), MemoryModuleType.HOME)), 
				Pair.of(3, new SleepTask()), 
				Pair.of(5, new RandomTask(ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleState.VALUE_ABSENT), ImmutableList.of(
					Pair.of(WalkHomeTask.create(speed), 1), 
					Pair.of(WanderIndoorsTask.create(speed), 4), 
					Pair.of(SurinaBrainTasks.createGoToPointOfInterestTask(speed, 4), 2), 
					Pair.of(new WaitTask(20, 40), 2)))), 
				SurinaBrainTasks.createBusyFollowTask(), 
				Pair.of(99, ScheduleActivityTask.create()));
	}
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super SurinaEntity>>> createMeetTasks(VillagerProfession profession, float speed)
	{
		/*
		 * ImmutableList.of(
			 * Pair.of(2, Tasks.pickRandomly(ImmutableList.of(
				 * Pair.of(GoToIfNearbyTask.create(MemoryModuleType.MEETING_POINT, 0.4f, 40), 2), 
				 * Pair.of(MeetVillagerTask.create(), 2)))), 
			 * Pair.of(10, new HoldTradeOffersTask(400, 1600)), 
			 * Pair.of(10, FindInteractionTargetTask.create(EntityType.PLAYER, 4)), 
			 * Pair.of(2, VillagerWalkTowardsTask.create(MemoryModuleType.MEETING_POINT, speed, 6, 100, 200)), 
			 * Pair.of(3, new GiveGiftsToHeroTask(100)), 
			 * Pair.of(3, ForgetCompletedPointOfInterestTask.create(poiType -> poiType.matchesKey(PointOfInterestTypes.MEETING), MemoryModuleType.MEETING_POINT)), 
			 * Pair.of(3, new CompositeTask(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), CompositeTask.Order.ORDERED, CompositeTask.RunMode.RUN_ONE, ImmutableList.of(Pair.of(new GatherItemsVillagerTask(), 1)))), 
			 * VillagerTaskListProvider.createFreeFollowTask(), 
			 * Pair.of(99, ScheduleActivityTask.create()));
		 */
		return ImmutableList.of(
				Pair.of(2, Tasks.pickRandomly(ImmutableList.of(
					Pair.of(GoToIfNearbyTask.create(MemoryModuleType.MEETING_POINT, JOB_WALKING_SPEED, 40), 2), 
					Pair.of(MeetVillagerTask.create(), 2)))), 
				Pair.of(10, FindInteractionTargetTask.create(EntityType.PLAYER, 4)), 
				Pair.of(2, SurinaBrainTasks.createWalkTowards(MemoryModuleType.MEETING_POINT, speed, 6, 100, 200)),
				Pair.of(3, ForgetCompletedPointOfInterestTask.create(poiType -> poiType.matchesKey(PointOfInterestTypes.MEETING), MemoryModuleType.MEETING_POINT)), 
				SurinaBrainTasks.createFreeFollowTask(), 
				Pair.of(99, ScheduleActivityTask.create()));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ImmutableList<Pair<Integer, ? extends Task<? super SurinaEntity>>> createIdleTasks(VillagerProfession profession, float speed)
	{
		/*
		 * ImmutableList.of(
			 * Pair.of(2, new RandomTask(ImmutableList.of(
				 * Pair.of(FindEntityTask.create(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 2), 
				 * Pair.of(FindEntityTask.create(EntityType.VILLAGER, 8, PassiveEntity::isReadyToBreed, PassiveEntity::isReadyToBreed, MemoryModuleType.BREED_TARGET, speed, 2), 1), 
				 * Pair.of(FindEntityTask.create(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 1), 
				 * Pair.of(FindWalkTargetTask.create(speed), 1), 
				 * Pair.of(GoTowardsLookTargetTask.create(speed, 2), 1), 
				 * Pair.of(new JumpInBedTask(speed), 1), 
				 * Pair.of(new WaitTask(30, 60), 1)))), 
			 * Pair.of(3, new GiveGiftsToHeroTask(100)), 
			 * Pair.of(3, FindInteractionTargetTask.create(EntityType.PLAYER, 4)), 
			 * Pair.of(3, new HoldTradeOffersTask(400, 1600)), 
			 * Pair.of(3, new CompositeTask(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), CompositeTask.Order.ORDERED, CompositeTask.RunMode.RUN_ONE, ImmutableList.of(Pair.of(new GatherItemsVillagerTask(), 1)))), 
			 * Pair.of(3, new CompositeTask(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.BREED_TARGET), CompositeTask.Order.ORDERED, CompositeTask.RunMode.RUN_ONE, ImmutableList.of(Pair.of(new VillagerBreedTask(), 1)))), 
			 * VillagerTaskListProvider.createFreeFollowTask(), 
			 * Pair.of(99, ScheduleActivityTask.create()));
		 */
		return ImmutableList.of(
				Pair.of(2, new RandomTask(ImmutableList.of(
					Pair.of(FindEntityTask.create(HOEntityTypes.SURINA.get(), 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 2),  
					Pair.of(FindEntityTask.create(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, speed, 2), 1), 
					Pair.of(FindWalkTargetTask.create(speed), 1), 
					Pair.of(GoTowardsLookTargetTask.create(speed, 2), 1), 
					Pair.of(new JumpInBedTask(speed), 1), 
					Pair.of(new WaitTask(30, 60), 1)))), 
				Pair.of(3, FindInteractionTargetTask.create(EntityType.PLAYER, 4)), 
				SurinaBrainTasks.createFreeFollowTask(), 
				Pair.of(99, ScheduleActivityTask.create()));
	}
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super SurinaEntity>>> createPanicTasks(VillagerProfession profession, float speed)
	{
		/*
		 * ImmutableList.of(
			 * Pair.of(0, StopPanickingTask.create()), 
			 * Pair.of(1, GoToRememberedPositionTask.createEntityBased(MemoryModuleType.NEAREST_HOSTILE, f, 6, false)), 
			 * Pair.of(1, GoToRememberedPositionTask.createEntityBased(MemoryModuleType.HURT_BY_ENTITY, f, 6, false)), 
			 * Pair.of(3, FindWalkTargetTask.create(f, 2, 2)), 
			 * VillagerTaskListProvider.createBusyFollowTask());
		 */
		float f = speed * 1.5f;
		return ImmutableList.of(
				Pair.of(0, StopPanickingTask.create()), 
				Pair.of(1, GoToRememberedPositionTask.createEntityBased(MemoryModuleType.NEAREST_HOSTILE, f, 6, false)), 
				Pair.of(1, GoToRememberedPositionTask.createEntityBased(MemoryModuleType.HURT_BY_ENTITY, f, 6, false)), 
				Pair.of(3, FindWalkTargetTask.create(f, 2, 2)), 
				SurinaBrainTasks.createBusyFollowTask());
	}
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super SurinaEntity>>> createHideTasks(VillagerProfession profession, float speed)
	{
		/*
		 * ImmutableList.of(
			 * Pair.of(0, ForgetBellRingTask.create(15, 3)), 
			 * Pair.of(1, HideInHomeTask.create(32, speed * 1.25f, 2)), 
			 * VillagerTaskListProvider.createBusyFollowTask());
		 */
		return ImmutableList.of(
				Pair.of(0, ForgetBellRingTask.create(15, 3)), 
				Pair.of(1, HideInHomeTask.create(32, speed * 1.25f, 2)), 
				SurinaBrainTasks.createBusyFollowTask());
	}
}
