package com.lying.init;

import java.util.function.Supplier;

import com.lying.Hrrmowners;
import com.lying.block.FirmamentBlock;
import com.lying.reference.Reference;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;

public class HOBlocks
{
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.BLOCK);
	private static int tally = 0;
	
	public static final RegistrySupplier<Block> SAND_FIRMAMENT	= register("sand_firmament", () -> new FirmamentBlock(AbstractBlock.Settings.create().mapColor(MapColor.YELLOW).sounds(BlockSoundGroup.SAND).instrument(NoteBlockInstrument.BASS).strength(2.0f)));
	
	private static RegistrySupplier<Block> register(String nameIn, Supplier<Block> blockIn)
	{
		++tally;
		return BLOCKS.register(Reference.ModInfo.prefix(nameIn), blockIn);
	}
	
	public static void init()
	{
		BLOCKS.register();
		Hrrmowners.LOGGER.info(" # Registered {} blocks", tally);
	}
}
