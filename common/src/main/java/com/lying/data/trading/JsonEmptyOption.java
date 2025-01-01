package com.lying.data.trading;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lying.init.HOSurinaTrades.TradeFactory;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;

public class JsonEmptyOption implements TradeFactory
{
	public static final Identifier ID = Identifier.of("empty");
	
	public Identifier id() { return ID; }
	
	public TradeOffer create(Entity var1, Random var2) { return null; }
	
	public JsonElement writeToJson() { return tradeToJson(this); }
	
	public TradeFactory fromJson(JsonElement element) { return tradeFromJson(element); }
	
	public static JsonElement tradeToJson(TradeFactory factory) { return new JsonObject(); }
	
	public static TradeFactory tradeFromJson(JsonElement element) { return new JsonEmptyOption(); }
}
