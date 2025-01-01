package com.lying.data.trading;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.lying.init.HOSurinaTrades.TradeFactory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.world.World;

/** JSON-storeable version of {@link TradeOffers.SellItemFactory} */
public class JsonSellOption implements TradeFactory
{
	public static final Codec<JsonSellOption> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			ItemStack.CODEC.fieldOf("stack").forGetter(o -> o.sell),
			Codec.INT.fieldOf("price").forGetter(o -> o.price),
			Codec.INT.fieldOf("maxUses").forGetter(o -> o.maxUses),
			Codec.INT.fieldOf("experience").forGetter(o -> o.experience),
			Codec.FLOAT.fieldOf("multiplier").forGetter(o -> o.multiplier),
			RegistryKey.createCodec(RegistryKeys.ENCHANTMENT_PROVIDER).optionalFieldOf("Enchantment").forGetter(o -> o.enchantmentProviderKey)
			)
				.apply(instance, (stack, price, maxUses, experience, multiplier, enchantmentKey) -> new JsonSellOption(stack, price, maxUses, experience, multiplier, enchantmentKey)));
	
	public static final Identifier ID = Identifier.of("sell_item");
	private final ItemStack sell;
	private final int price;
	private final int maxUses;
	private final int experience;
	private final float multiplier;
	private final Optional<RegistryKey<EnchantmentProvider>> enchantmentProviderKey;
	
	public JsonSellOption()
	{
		this(Blocks.STONE, 1, 1, 0, 0);
	}
	
	public JsonSellOption(Block block, int price, int count, int maxUses, int experience)
	{
		this(new ItemStack(block), price, count, maxUses, experience);
	}
	
	public JsonSellOption(Item item, int price, int count, int experience)
	{
		this(new ItemStack(item), price, count, 12, experience);
	}
	
	public JsonSellOption(Item item, int price, int count, int maxUses, int experience)
	{
		this(new ItemStack(item), price, count, maxUses, experience);
	}
	
	public JsonSellOption(ItemStack stack, int price, int count, int maxUses, int experience)
	{
		this(stack, price, count, maxUses, experience, 0.05f);
	}
	
	public JsonSellOption(Item item, int price, int count, int maxUses, int experience, float multiplier)
	{
		this(new ItemStack(item), price, count, maxUses, experience, multiplier);
	}
	
	public JsonSellOption(Item item, int price, int count, int maxUses, int experience, float multiplier, RegistryKey<EnchantmentProvider> enchantmentProviderKey)
	{
		this(new ItemStack(item), price, count, maxUses, experience, multiplier, Optional.of(enchantmentProviderKey));
	}
	
	public JsonSellOption(ItemStack stack, int price, int count, int maxUses, int experience, float multiplier)
	{
		this(stack, price, count, maxUses, experience, multiplier, Optional.empty());
	}
	
	public JsonSellOption(ItemStack sell, int price, int count, int maxUses, int experience, float multiplier, Optional<RegistryKey<EnchantmentProvider>> enchantmentProviderKey)
	{
		this.sell = sell;
		this.sell.setCount(count);
		
		this.price = price;
		this.maxUses = maxUses;
		this.experience = experience;
		this.multiplier = multiplier;
		this.enchantmentProviderKey = enchantmentProviderKey;
	}
	
	public JsonSellOption(ItemStack sell, int price, int maxUses, int experience, float multiplier, Optional<RegistryKey<EnchantmentProvider>> enchantmentProviderKey)
	{
		this.sell = sell;
		this.price = price;
		this.maxUses = maxUses;
		this.experience = experience;
		this.multiplier = multiplier;
		this.enchantmentProviderKey = enchantmentProviderKey;
	}
	
	public TradeOffer create(Entity entity, Random random)
	{
		ItemStack itemStack = this.sell.copy();
		World world = entity.getWorld();
		this.enchantmentProviderKey.ifPresent(key -> EnchantmentHelper.applyEnchantmentProvider(itemStack, world.getRegistryManager(), (RegistryKey<EnchantmentProvider>)key, world.getLocalDifficulty(entity.getBlockPos()), random));
		return new TradeOffer(new TradedItem((ItemConvertible)Items.EMERALD, this.price), itemStack, this.maxUses, this.experience, this.multiplier);
	}
	
	public Identifier id() { return ID; }
	
	public JsonElement writeToJson() { return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow(); }
	
	public TradeFactory fromJson(JsonElement element) { return CODEC.parse(JsonOps.INSTANCE, element.getAsJsonObject()).getOrThrow(); }
}