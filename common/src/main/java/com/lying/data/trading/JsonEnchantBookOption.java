package com.lying.data.trading;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.lying.init.HOSurinaTrades.TradeFactory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;

public class JsonEnchantBookOption implements TradeFactory
{
	public static final Codec<JsonEnchantBookOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("experience").forGetter(o -> o.experience),
			Codec.INT.fieldOf("min_level").forGetter(o -> o.minLevel),
			Codec.INT.fieldOf("max_level").forGetter(o -> o.maxLevel),
			TagKey.codec(RegistryKeys.ENCHANTMENT).fieldOf("enchantments").forGetter(o -> o.possibleEnchantments)
			)
				.apply(instance, (experience, minLevel, maxLevel, possibleEnchantments) -> new JsonEnchantBookOption(experience, minLevel, maxLevel, possibleEnchantments)));
	
	public static final Identifier ID = Identifier.of("enchant_book");
	private final TagKey<Enchantment> possibleEnchantments;
	private final int experience;
	private final int minLevel;
	private final int maxLevel;
	
	public JsonEnchantBookOption()
	{
		this(0, EnchantmentTags.TRADEABLE);
	}
	
	public JsonEnchantBookOption(int experience, TagKey<Enchantment> possibleEnchantments)
	{
		this(experience, 0, Integer.MAX_VALUE, possibleEnchantments);
	}
	
	public JsonEnchantBookOption(int experience, int minLevel, int maxLevel, TagKey<Enchantment> possibleEnchantments)
	{
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.experience = experience;
		this.possibleEnchantments = possibleEnchantments;
	}
	
	public Identifier id() { return ID; }
	
	public TradeOffer create(Entity entity, Random random)
	{
		int l;
		ItemStack itemStack;
		Optional<RegistryEntry<Enchantment>> optional = entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getRandomEntry(this.possibleEnchantments, random);
		if(!optional.isEmpty())
		{
			RegistryEntry<Enchantment> registryEntry = optional.get();
			Enchantment enchantment = registryEntry.value();
			int i = Math.max(enchantment.getMinLevel(), this.minLevel);
			int j = Math.min(enchantment.getMaxLevel(), this.maxLevel);
			int k = MathHelper.nextInt(random, i, j);
			itemStack = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(registryEntry, k));
			l = 2 + random.nextInt(5 + k * 10) + 3 * k;
			if(registryEntry.isIn(EnchantmentTags.DOUBLE_TRADE_PRICE))
				l *= 2;
			if(l > 64)
				l = 64;
		}
		else
		{
			l = 1;
			itemStack = new ItemStack(Items.BOOK);
		}
		return new TradeOffer(new TradedItem(Items.EMERALD, l), Optional.of(new TradedItem(Items.BOOK)), itemStack, 12, this.experience, 0.2f);
	}
	
	public JsonElement writeToJson() { return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow(); }
	
	public TradeFactory fromJson(JsonElement element) { return CODEC.parse(JsonOps.INSTANCE, element.getAsJsonObject()).getOrThrow(); }
}
