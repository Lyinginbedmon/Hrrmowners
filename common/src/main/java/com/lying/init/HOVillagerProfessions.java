package com.lying.init;

import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import com.lying.Hrrmowners;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

public class HOVillagerProfessions
{
	public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.VILLAGER_PROFESSION);
	private static int tally = 0;
	
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
	
	private static Function<String, VillagerProfession> profession(RegistryKey<PointOfInterestType> poi, SoundEvent soundIn)
	{
		return id -> new VillagerProfession(id, entry -> entry.matchesKey(poi), entry -> entry.matchesKey(poi), ImmutableSet.of(), ImmutableSet.of(), soundIn);
	}
}
