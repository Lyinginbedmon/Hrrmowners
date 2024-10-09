package com.lying.entity;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.lying.Hrrmowners;
import com.lying.entity.ai.SurinaTaskListProvider;
import com.lying.init.HOItems;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

public class SurinaEntity extends MerchantEntity implements VillagerDataContainer
{
	private static final Logger LOGGER = Hrrmowners.LOGGER;
	private static final TrackedData<VillagerData> VILLAGER_DATA = DataTracker.registerData(SurinaEntity.class, TrackedDataHandlerRegistry.VILLAGER_DATA);
	private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.HOME, MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.MEETING_POINT, MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, new MemoryModuleType[]{MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.BREED_TARGET, MemoryModuleType.PATH, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_BED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_HOSTILE, MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleType.HIDING_PLACE, MemoryModuleType.HEARD_BELL_TIME, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.LAST_SLEPT, MemoryModuleType.LAST_WOKEN, MemoryModuleType.LAST_WORKED_AT_POI, MemoryModuleType.GOLEM_DETECTED_RECENTLY});
	private static final ImmutableList<SensorType<? extends Sensor<? super SurinaEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_BED, SensorType.HURT_BY, SensorType.VILLAGER_HOSTILES, SensorType.VILLAGER_BABIES, SensorType.GOLEM_DETECTED);
	public static final Map<MemoryModuleType<GlobalPos>, BiPredicate<SurinaEntity, RegistryEntry<PointOfInterestType>>> POINTS_OF_INTEREST = ImmutableMap.of(MemoryModuleType.HOME, (villager, arg2) -> arg2.matchesKey(PointOfInterestTypes.HOME), MemoryModuleType.JOB_SITE, (villager, arg2) -> villager.getVillagerData().getProfession().heldWorkstation().test((RegistryEntry<PointOfInterestType>)arg2), MemoryModuleType.POTENTIAL_JOB_SITE, (villager, arg2) -> VillagerProfession.IS_ACQUIRABLE_JOB_SITE.test((RegistryEntry<PointOfInterestType>)arg2), MemoryModuleType.MEETING_POINT, (villager, arg2) -> arg2.matchesKey(PointOfInterestTypes.MEETING));
	private boolean natural;
	
	public SurinaEntity(EntityType<? extends MerchantEntity> entityType, World world)
	{
		super(entityType, world);
		((MobNavigation)getNavigation()).setCanPathThroughDoors(true);
		getNavigation().setCanSwim(true);
		setVillagerData(getVillagerData().withType(VillagerType.DESERT).withProfession(VillagerProfession.NONE));
	}
	
	@SuppressWarnings("unchecked")
	public Brain<SurinaEntity> getBrain() { return (Brain<SurinaEntity>) super.getBrain(); }
	
	protected Brain.Profile<SurinaEntity> createBrainProfile()
	{
		return Brain.createProfile(MEMORY_MODULES, SENSORS);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Brain<?> deserializeBrain(Dynamic<?> dynamic)
	{
		Brain brain = createBrainProfile().deserialize(dynamic);
		initBrain((Brain<SurinaEntity>)brain);
		return brain;
	}
	
	public void reinitializeBrain(ServerWorld world)
	{
		Brain<SurinaEntity> brain = getBrain();
		brain.stopAllTasks(world, this);
		this.brain = brain.copy();
		initBrain(getBrain());
	}
	
	private void initBrain(Brain<SurinaEntity> brain)
	{
		VillagerProfession profession = this.getVillagerData().getProfession();
		if(this.isBaby())
		{
			brain.setSchedule(Schedule.VILLAGER_BABY);
			brain.setTaskList(Activity.PLAY, SurinaTaskListProvider.createPlayTasks((float)0.5f));
		}
		else
		{
			brain.setSchedule(Schedule.VILLAGER_DEFAULT);
			brain.setTaskList(Activity.WORK, SurinaTaskListProvider.createWorkTasks(profession, (float)0.5f), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleState.VALUE_PRESENT)));
		}
		brain.setTaskList(Activity.CORE, SurinaTaskListProvider.createCoreTasks(profession, 0.5f));
		brain.setTaskList(Activity.MEET, SurinaTaskListProvider.createMeetTasks(profession, 0.5f), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleState.VALUE_PRESENT)));
		brain.setTaskList(Activity.REST, SurinaTaskListProvider.createRestTasks(profession, 0.5f));
		brain.setTaskList(Activity.IDLE, SurinaTaskListProvider.createIdleTasks(profession, 0.5f));
		brain.setTaskList(Activity.PANIC, SurinaTaskListProvider.createPanicTasks(profession, 0.5f));
		brain.setTaskList(Activity.HIDE, SurinaTaskListProvider.createHideTasks(profession, 0.5f));
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.doExclusively(Activity.IDLE);
		brain.refreshActivities(getWorld().getTimeOfDay(), getWorld().getTime());
	}
	
	protected void onGrowUp()
	{
		super.onGrowUp();
		if(!getWorld().isClient())
			reinitializeBrain((ServerWorld)getWorld());
	}
	
	protected void initDataTracker(DataTracker.Builder builder)
	{
		super.initDataTracker(builder);
		builder.add(VILLAGER_DATA, new VillagerData(VillagerType.DESERT, VillagerProfession.NITWIT, 1));
	}
	
	public static DefaultAttributeContainer.Builder createSurinaAttributes()
	{
		return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
	}
	
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, getVillagerData()).resultOrPartial(LOGGER::error).ifPresent(arg2 -> nbt.put("VillagerData", arg2));
		if(natural)
			nbt.putBoolean("AssignProfessionWhenSpawned", true);
	}
	
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		if(nbt.contains("VillagerData", NbtElement.COMPOUND_TYPE))
			VillagerData.CODEC.parse(NbtOps.INSTANCE, nbt.get("VillagerData")).resultOrPartial(LOGGER::error).ifPresent(villagerData -> this.dataTracker.set(VILLAGER_DATA, villagerData));
		if(!getWorld().isClient())
			reinitializeBrain((ServerWorld)getWorld());
		if(nbt.contains("AssignProfessionWhenSpawned"))
			this.natural = nbt.getBoolean("AssignProfessionWhenSpawned");
	}
	
	public boolean canImmediatelyDespawn(double distSqr) { return false; }
	
	public boolean isNatural() { return natural; }
	
	protected void mobTick()
	{
		getWorld().getProfiler().push("surinaBrain");
		getBrain().tick((ServerWorld)getWorld(), this);
		getWorld().getProfiler().pop();
		if(natural)
			natural = false;
		super.mobTick();
	}
	
	public ActionResult interactMob(PlayerEntity player, Hand hand)
	{
		ItemStack stack = player.getStackInHand(hand);
		if(stack.isOf(HOItems.SURINA_SPAWN_EGG.get()) || !isAlive() || hasCustomer() || isSleeping() || player.shouldCancelInteraction())
			return super.interactMob(player, hand);
		
		boolean isClient = getWorld().isClient();
		if(isBaby())
		{
			sayNo();
			return ActionResult.success(isClient);
		}
		if(!isClient)
		{
			boolean noOffers = getOffers().isEmpty();
			if(hand == Hand.MAIN_HAND)
			{
				if(noOffers)
					sayNo();
				player.incrementStat(Stats.TALKED_TO_VILLAGER);
			}
			if(noOffers)
				return ActionResult.CONSUME;
			beginTradeWith(player);
		}
		return ActionResult.success(isClient);
	}
	
	private void sayNo()
	{
		setHeadRollingTimeLeft(40);
		if(!getWorld().isClient())
			playSound(SoundEvents.ENTITY_VILLAGER_NO);
	}
	
	private void beginTradeWith(PlayerEntity customer)
	{
		prepareOffersFor(customer);
		setCustomer(customer);
		sendOffers(customer, getDisplayName(), getVillagerData().getLevel());
	}
	
	private void prepareOffersFor(PlayerEntity player)
	{
		if(player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE))
		{
			StatusEffectInstance inst = player.getStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);
			double amp = inst.getAmplifier();
			for(TradeOffer offer : getOffers())
			{
				double d = 0.3 + 0.0625 * amp;
				int j = (int)Math.floor(d * (double)offer.getOriginalFirstBuyItem().getCount());
				offer.increaseSpecialPrice(-Math.max(j, 1));
			}
		}
	}
	
	protected void afterUsing(TradeOffer tradeOffer)
	{
		int i = 3 + this.random.nextInt(4);
		if(tradeOffer.shouldRewardPlayerExperience())
			getWorld().spawnEntity(new ExperienceOrbEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), i));
	}
	
	protected void fillRecipes()
	{
		TradeOffers.Factory[] tradeOfferListings;
		Int2ObjectMap<TradeOffers.Factory[]> int2objectmap1;
		VillagerData villagerData = getVillagerData();
		Int2ObjectMap<TradeOffers.Factory[]> int2objectmap = getWorld().getEnabledFeatures().contains(FeatureFlags.TRADE_REBALANCE) ? ((int2objectmap1 = TradeOffers.REBALANCED_PROFESSION_TO_LEVELED_TRADE.get(villagerData.getProfession())) != null ? int2objectmap1 : TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(villagerData.getProfession())) : TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(villagerData.getProfession());
		if(int2objectmap != null && !int2objectmap.isEmpty() && (tradeOfferListings = (TradeOffers.Factory[])int2objectmap.get(villagerData.getLevel())) != null)
			fillRecipesFromPool(getOffers(), tradeOfferListings, 2);
	}
	
	/** Surina are not spawned by binary reproduction, but spawned in the brood chamber */
	public PassiveEntity createChild(ServerWorld var1, PassiveEntity var2) { return null; }
	
	protected SoundEvent getAmbientSound()
	{
		if(isSleeping())
			return null;
		
		return hasCustomer() ? SoundEvents.ENTITY_VILLAGER_TRADE : SoundEvents.ENTITY_VILLAGER_AMBIENT;
	}
	
	protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_VILLAGER_HURT; }
	
	protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_VILLAGER_DEATH; }
	
	public void playWorkSound() { playSound(getVillagerData().getProfession().workSound()); }
	
	public VillagerData getVillagerData() { return getDataTracker().get(VILLAGER_DATA); }
	
	public void setVillagerData(VillagerData villagerData)
	{
		VillagerData existing = getVillagerData();
		if(existing.getProfession() != villagerData.getProfession())
			this.offers = null;
		getDataTracker().set(VILLAGER_DATA, villagerData);
	}
	
	public void onDeath(DamageSource damageSource)
	{
		releaseAllTickets();
		super.onDeath(damageSource);
	}
	
	private void releaseAllTickets()
	{
		releaseTicketFor(MemoryModuleType.HOME);
		releaseTicketFor(MemoryModuleType.JOB_SITE);
		releaseTicketFor(MemoryModuleType.POTENTIAL_JOB_SITE);
		releaseTicketFor(MemoryModuleType.MEETING_POINT);
	}
	
	public void releaseTicketFor(MemoryModuleType<GlobalPos> pos)
	{
		if(getWorld() instanceof ServerWorld)
		{
			MinecraftServer minecraftserver = ((ServerWorld)this.getWorld()).getServer();
			this.brain.getOptionalRegisteredMemory(pos).ifPresent(posx -> 
			{
				ServerWorld serverlevel = minecraftserver.getWorld((RegistryKey<World>)posx.dimension());
				if(serverlevel != null)
				{
					PointOfInterestStorage poimanager = serverlevel.getPointOfInterestStorage();
					Optional<RegistryEntry<PointOfInterestType>> optional = poimanager.getType(posx.pos());
					BiPredicate<SurinaEntity, RegistryEntry<PointOfInterestType>> bipredicate = POINTS_OF_INTEREST.get(pos);
					if(optional.isPresent() && bipredicate.test(this, optional.get()))
					{
						poimanager.releaseTicket(posx.pos());
						DebugInfoSender.sendPointOfInterest((ServerWorld)serverlevel, (BlockPos)posx.pos());
					}
				}
			});
		}
	}
	
	public void handleStatus(byte status)
	{
		switch(status)
		{
			case 12:
				produceParticles(ParticleTypes.HEART);
				break;
			case 13:
				produceParticles(ParticleTypes.ANGRY_VILLAGER);
				break;
			case 14:
				produceParticles(ParticleTypes.HAPPY_VILLAGER);
				break;
			case 42:
				produceParticles(ParticleTypes.SPLASH);
				break;
			default:
				super.handleStatus(status);
				break;
		}
	}
}