package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.function.Supplier;

import com.lying.Hrrmowners;
import com.lying.reference.Reference;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class HOItems
{
	public static final DeferredRegister<ItemGroup> TABS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ITEM_GROUP);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Reference.ModInfo.MOD_ID, RegistryKeys.ITEM);
	private static int tally = 0;
	
	public static final RegistrySupplier<ItemGroup> HRRMOWNERS_TAB = TABS.register(Reference.ModInfo.MOD_ID, () -> CreativeTabRegistry.create(
			Text.translatable("itemGroup."+Reference.ModInfo.MOD_ID+".item_group"), 
			() -> new ItemStack(HOItems.SURINA_SPAWN_EGG.get())));
	
	public static final RegistrySupplier<Item> SURINA_SPAWN_EGG	= register("surina_spawn_egg", () -> new SpawnEggItem(HOEntityTypes.SURINA.get(), 0xD8CFB0, 0xA19264, new Item.Settings().arch$tab(HRRMOWNERS_TAB)));
	
	public static final RegistrySupplier<Item> SAND_FIRMAMENT_BLOCK	= registerBlockItem("sand_firmament", HOBlocks.SAND_FIRMAMENT);
	public static final RegistrySupplier<Item> NEST_BLOCK		= registerBlockItem("nest", HOBlocks.NEST);
	
	public static final RegistrySupplier<Item> BLAST_FURNACE	= registerBlockItem("surina_blast_furnace", HOBlocks.BLAST_FURNACE);
	public static final RegistrySupplier<Item> CART_TABLE		= registerBlockItem("surina_cartography_table", HOBlocks.CART_TABLE);
	public static final RegistrySupplier<Item> COMPOSTER		= registerBlockItem("surina_composter", HOBlocks.COMPOSTER);
	public static final RegistrySupplier<Item> GRINDSTONE		= registerBlockItem("surina_grindstone", HOBlocks.GRINDSTONE);
	public static final RegistrySupplier<Item> LECTERN			= registerBlockItem("surina_lectern", HOBlocks.LECTERN);
	public static final RegistrySupplier<Item> LOOM				= registerBlockItem("surina_loom", HOBlocks.LOOM);
	public static final RegistrySupplier<Item> SMOKER			= registerBlockItem("surina_smoker", HOBlocks.SMOKER);
	public static final RegistrySupplier<Item> STONECUTTER		= registerBlockItem("surina_stonecutter", HOBlocks.STONECUTTER);
	
	private static RegistrySupplier<Item> registerBlockItem(String nameIn, RegistrySupplier<Block> blockIn)
	{
		return register(nameIn, () -> new BlockItem(blockIn.get(), new Item.Settings().arch$tab(HRRMOWNERS_TAB)));
	}
	
	private static RegistrySupplier<Item> register(String nameIn, Supplier<Item> itemIn)
	{
		++tally;
		return ITEMS.register(prefix(nameIn), itemIn);
	}
	
	public static void init()
	{
		ITEMS.register();
		TABS.register();
		Hrrmowners.LOGGER.info(" # Registered {} items", tally);
	}
}
