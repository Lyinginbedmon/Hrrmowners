package com.lying.init;

import java.util.Optional;
import java.util.function.Supplier;

import com.lying.Hrrmowners;
import com.lying.reference.Reference;
import com.mojang.serialization.Codec;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.GlobalPos;

public class HOMemoryModuleTypes
{
	private static final DeferredRegister<MemoryModuleType<?>> MODULES = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.MEMORY_MODULE_TYPE);
	private static int tally = 0;
	
	public static final RegistrySupplier<MemoryModuleType<GlobalPos>> HOA_TASK	= register("hoa_task", GlobalPos.CODEC);
	public static final RegistrySupplier<MemoryModuleType<Boolean>> RECEIVING_TASK	= register("receiving_task", Codec.BOOL);
	
	private static <T> RegistrySupplier<MemoryModuleType<T>> register(String nameIn, Codec<T> codecIn)
	{
		return register(nameIn, () -> new MemoryModuleType<T>(Optional.of(codecIn)));
	}
	
	private static <T> RegistrySupplier<MemoryModuleType<T>> register(String nameIn, Supplier<MemoryModuleType<T>> blockIn)
	{
		++tally;
		return MODULES.register(Reference.ModInfo.prefix(nameIn), blockIn);
	}
	
	public static void init()
	{
		MODULES.register();
		Hrrmowners.LOGGER.info(" # Registered {} AI memory modules", tally);
	}
}
