package com.lying.data.trading;

import java.util.List;

import com.google.gson.JsonElement;
import com.lying.init.HOSurinaTrades.TradeFactory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;

public class JsonSellSuspiciousStewOption implements TradeFactory
{
	public static final Codec<JsonSellSuspiciousStewOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SuspiciousStewEffectsComponent.CODEC.fieldOf("effects").forGetter(o -> o.stewEffects),
			Codec.INT.fieldOf("experience").forGetter(o -> o.experience),
			Codec.FLOAT.fieldOf("multiplier").forGetter(o -> o.multiplier)
			)
				.apply(instance, (stewEffects, experience, multiplier) -> new JsonSellSuspiciousStewOption(stewEffects, experience, multiplier)));
	
	public static final Identifier ID = Identifier.of("sell_suspicious_stew");
	private final SuspiciousStewEffectsComponent stewEffects;
	private final int experience;
	private final float multiplier;
	
	public JsonSellSuspiciousStewOption()
	{
		this(new SuspiciousStewEffectsComponent(List.of()), 0, 0F);
	}
	
	public JsonSellSuspiciousStewOption(RegistryEntry<StatusEffect> effect, int duration, int experience)
	{
		this(new SuspiciousStewEffectsComponent(List.of(new SuspiciousStewEffectsComponent.StewEffect(effect, duration))), experience, 0.05F);
	}
	
	public JsonSellSuspiciousStewOption(SuspiciousStewEffectsComponent stewEffects, int experience, float multiplier)
	{
		this.stewEffects = stewEffects;
		this.experience = experience;
		this.multiplier = multiplier;
	}
	
	public Identifier id() { return ID; }
	
	public TradeOffer create(Entity entity, Random random)
	{
		ItemStack itemStack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
		itemStack.set(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS, this.stewEffects);
		return new TradeOffer(new TradedItem(Items.EMERALD), itemStack, 12, this.experience, this.multiplier);
	}
	
	public JsonElement writeToJson() { return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow(); }
	
	public TradeFactory fromJson(JsonElement element) { return CODEC.parse(JsonOps.INSTANCE, element.getAsJsonObject()).getOrThrow(); }
}
