package com.lying.init;

import static com.lying.reference.Reference.ModInfo.prefix;

import java.util.function.Supplier;

import com.lying.Hrrmowners;
import com.lying.reference.Reference;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
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
	
	public static final RegistrySupplier<ItemGroup> HRRMOWNERS_TAB = TABS.register(Reference.ModInfo.MOD_ID, () -> CreativeTabRegistry.create(
			Text.translatable("itemGroup."+Reference.ModInfo.MOD_ID+".item_group"), 
			() -> new ItemStack(HOItems.SURINA_SPAWN_EGG.get())));
	
	public static final RegistrySupplier<Item> SURINA_SPAWN_EGG	= register("surina_spawn_egg", () -> new SpawnEggItem(HOEntityTypes.SURINA.get(), 0xD8CFB0, 0xA19264, new Item.Settings().arch$tab(HRRMOWNERS_TAB)));
	
	private static RegistrySupplier<Item> register(String nameIn, Supplier<Item> itemIn)
	{
		return ITEMS.register(prefix(nameIn), itemIn);
	}
	
	public static void init()
	{
		ITEMS.register();
		TABS.register();
		Hrrmowners.LOGGER.info(" # Registered items");
	}
}
