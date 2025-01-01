package com.lying.data.trading;

import com.google.gson.JsonElement;
import com.lying.init.HOSurinaTrades.TradeFactory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;

/** JSON-storeable version of {@link TradeOffers.BuyItemFactory} */
public class JsonBuyOption implements TradeFactory
{
	public static final Codec<JsonBuyOption> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			TradedItem.CODEC.fieldOf("stack").forGetter(o -> o.stack),
			Codec.INT.fieldOf("maxUses").forGetter(o -> o.maxUses),
			Codec.INT.fieldOf("experience").forGetter(o -> o.experience),
			Codec.INT.fieldOf("price").forGetter(o -> o.price)
			)
				.apply(instance, (stack, maxUses, experience, price) -> new JsonBuyOption(stack, maxUses, experience, price)));
	
	public static final Identifier ID = Identifier.of("buy_item");
	private final TradedItem stack;
	private final int maxUses;
	private final int experience;
	private final int price;
	
	public JsonBuyOption()
	{
		this(Blocks.STONE, 1, 1, 0, 0);
	}
	
	public JsonBuyOption(ItemConvertible item, int count, int maxUses, int experience)
	{
		this(item, count, maxUses, experience, 1);
	}
	
	public JsonBuyOption(ItemConvertible item, int count, int maxUses, int experience, int price)
	{
		this(new TradedItem((ItemConvertible)item.asItem(), count), maxUses, experience, price);
	}
	
	public JsonBuyOption(TradedItem stack, int maxUses, int experience, int price)
	{
		this.stack = stack;
		this.maxUses = maxUses;
		this.experience = experience;
		this.price = price;
	}
	
	public TradeOffer create(Entity entity, Random random)
	{
		return new TradeOffer(this.stack, new ItemStack(Items.EMERALD, this.price), this.maxUses, this.experience, 0.05F);
	}
	
	public Identifier id() { return ID; }
	
	public JsonElement writeToJson() { return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow(); }
	
	public TradeFactory fromJson(JsonElement element) { return CODEC.parse(JsonOps.INSTANCE, element.getAsJsonObject()).getOrThrow(); }
}