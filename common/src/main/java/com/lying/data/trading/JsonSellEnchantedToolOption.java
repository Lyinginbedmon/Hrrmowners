package com.lying.data.trading;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.lying.init.HOSurinaTrades.TradeFactory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;

public class JsonSellEnchantedToolOption implements TradeFactory
{
	@SuppressWarnings("deprecation")
	public static final Codec<JsonSellEnchantedToolOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ItemStack.ITEM_CODEC.fieldOf("item").forGetter(o -> o.tool.getItem().getRegistryEntry()),
			Codec.INT.fieldOf("base_price").forGetter(o -> o.basePrice),
			Codec.INT.fieldOf("max_uses").forGetter(o -> o.maxUses),
			Codec.INT.fieldOf("experience").forGetter(o -> o.experience),
			Codec.FLOAT.fieldOf("multiplier").forGetter(o -> o.multiplier)
			)
				.apply(instance, (item, basePrice, maxUses, experience, multiplier) -> new JsonSellEnchantedToolOption(item.value(), basePrice, maxUses, experience, multiplier)));
	
	public static final Identifier ID = Identifier.of("sell_enchanted_tool");
	private final ItemStack tool;
	private final int basePrice;
	private final int maxUses;
	private final int experience;
	private final float multiplier;
	
	public JsonSellEnchantedToolOption()
	{
		this(Items.STONE, 1, 0, 0);
	}
	
	public JsonSellEnchantedToolOption(Item item, int basePrice, int maxUses, int experience)
	{
		this(item, basePrice, maxUses, experience, 0.05F);
	}
	
	public JsonSellEnchantedToolOption(Item item, int basePrice, int maxUses, int experience, float multiplier)
	{
		this.tool = new ItemStack(item);
		this.basePrice = basePrice;
		this.maxUses = maxUses;
		this.experience = experience;
		this.multiplier = multiplier;
	}
	
	public Identifier id() { return ID; }
	
	public TradeOffer create(Entity entity, Random random)
	{
		int i = 5 + random.nextInt(15);
		DynamicRegistryManager dynamicRegistryManager = entity.getWorld().getRegistryManager();
		Optional<RegistryEntryList.Named<Enchantment>> optional = dynamicRegistryManager.get(RegistryKeys.ENCHANTMENT).getEntryList(EnchantmentTags.ON_TRADED_EQUIPMENT);
		ItemStack itemStack = EnchantmentHelper.enchant(random, new ItemStack(this.tool.getItem()), i, dynamicRegistryManager, optional);
		int j = Math.min(this.basePrice + i, 64);
		TradedItem tradedItem = new TradedItem(Items.EMERALD, j);
		return new TradeOffer(tradedItem, itemStack, this.maxUses, this.experience, this.multiplier);
	}
	
	public JsonElement writeToJson() { return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow(); }
	
	public TradeFactory fromJson(JsonElement element) { return CODEC.parse(JsonOps.INSTANCE, element.getAsJsonObject()).getOrThrow(); }
}
