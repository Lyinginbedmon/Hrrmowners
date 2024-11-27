package com.lying.init;

import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;
import com.lying.Hrrmowners;
import com.lying.data.HOTags;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

public class HOVillagerProfessions
{
	public static final Predicate<RegistryEntry<PointOfInterestType>> IS_SURINA_JOB_SITE = poiType -> poiType.isIn(HOTags.SURINA_JOB_SITES);
	public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.VILLAGER_PROFESSION);
	private static int tally = 0;
	
	public static final RegistrySupplier<VillagerProfession> NEET	= register("neet", profession(PointOfInterestType.NONE, poiType -> poiType.isIn(HOTags.SURINA_JOB_SITES), null));
	public static final RegistrySupplier<VillagerProfession> QUEEN	= register("queen", profession(HOPointOfInterestTypes.NEST_KEY, SoundEvents.ENTITY_VILLAGER_WORK_CLERIC));
	
	private static RegistrySupplier<VillagerProfession> register(String nameIn, Function<String, VillagerProfession> blockIn)
	{
		++tally;
		return PROFESSIONS.register(Reference.ModInfo.prefix(nameIn), () -> blockIn.apply(nameIn));
	}
	
	public static void init()
	{
		PROFESSIONS.register();
		Hrrmowners.LOGGER.info(" # Registered {} villager professions", tally);
	}
	
	private static Function<String, VillagerProfession> profession(RegistryKey<PointOfInterestType> poi, @Nullable SoundEvent soundIn)
	{
		return profession(entry -> entry.matchesKey(poi), entry -> entry.matchesKey(poi), soundIn);
	}
	
	private static Function<String, VillagerProfession> profession(Predicate<RegistryEntry<PointOfInterestType>> heldWorkstation, Predicate<RegistryEntry<PointOfInterestType>> acquirableWorkstation, @Nullable SoundEvent workSound)
	{
		return profession(heldWorkstation, acquirableWorkstation, ImmutableSet.of(), ImmutableSet.of(), workSound);
	}
	
	@SuppressWarnings("unused")
	private static Function<String, VillagerProfession> profession(RegistryKey<PointOfInterestType> heldWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound)
	{
		return profession(entry -> entry.matchesKey(heldWorkstation), entry -> entry.matchesKey(heldWorkstation), gatherableItems, secondaryJobSites, workSound);
	}
	
	private static Function<String, VillagerProfession> profession(Predicate<RegistryEntry<PointOfInterestType>> heldWorkstation, Predicate<RegistryEntry<PointOfInterestType>> acquirableWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound)
	{
		return id -> new VillagerProfession(id, heldWorkstation, acquirableWorkstation, gatherableItems, secondaryJobSites, workSound);
	}
}
