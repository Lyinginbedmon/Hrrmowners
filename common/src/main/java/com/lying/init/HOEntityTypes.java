package com.lying.init;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.lying.Hrrmowners;
import com.lying.entity.SurinaEntity;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.registry.RegistryKeys;

public class HOEntityTypes
{
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ENTITY_TYPE);
	
	public static final RegistrySupplier<EntityType<SurinaEntity>> SURINA	= register("surina", () -> 
	{
		EntityType.Builder<SurinaEntity> builder = EntityType.Builder.<SurinaEntity>create(SurinaEntity::new, SpawnGroup.MISC).dimensions(0.6f, 1.95f).eyeHeight(1.62f).maxTrackingRange(10);
		return builder.build("surina");
	});
	
	private static <T extends Entity> RegistrySupplier<EntityType<T>> register(String name, Supplier<EntityType<T>> entry)
	{
		return ENTITY_TYPES.register(Reference.ModInfo.prefix(name), entry);
	}
	
	public static void init()
	{
		ENTITY_TYPES.register();
		Hrrmowners.LOGGER.info(" # Registered entity types");
	}
	
	public static void registerAttributeContainers(BiConsumer<EntityType<? extends LivingEntity>, DefaultAttributeContainer.Builder> funcIn)
	{
		funcIn.accept(SURINA.get(), SurinaEntity.createSurinaAttributes());
	}
}
