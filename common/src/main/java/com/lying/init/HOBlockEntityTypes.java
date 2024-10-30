package com.lying.init;

import java.util.function.Supplier;

import com.lying.Hrrmowners;
import com.lying.block.entity.NestBlockEntity;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BlockEntityType.Builder;
import net.minecraft.registry.RegistryKeys;

public class HOBlockEntityTypes
{
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES	= DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE);
	private static int tally = 0;
	
	public static final RegistrySupplier<BlockEntityType<NestBlockEntity>> NEST	= register("nest", () -> Builder.create(NestBlockEntity::new, HOBlocks.NEST.get()).build(null));
	
	private static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String nameIn, Supplier<BlockEntityType<T>> typeIn)
	{
		++tally;
		return BLOCK_ENTITIES.register(Reference.ModInfo.prefix(nameIn), typeIn);
	}
	
	public static void init()
	{
		BLOCK_ENTITIES.register();
		Hrrmowners.LOGGER.info(" # Registered {} block entity types", tally);
	}
}
