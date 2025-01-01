package com.lying.data.trading;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.lying.init.HOSurinaTrades.TradeFactory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.world.gen.structure.Structure;

public class JsonSellMapOption implements TradeFactory
{
	public static final Codec<JsonSellMapOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("price").forGetter(o -> o.price),
			TagKey.codec(RegistryKeys.STRUCTURE).fieldOf("structure").forGetter(o -> o.structure),
			Codec.STRING.fieldOf("name_key").forGetter(o -> o.nameKey),
			MapDecorationType.CODEC.fieldOf("decoration").forGetter(o -> o.decoration),
			Codec.INT.fieldOf("max_uses").forGetter(o -> o.maxUses),
			Codec.INT.fieldOf("experience").forGetter(o -> o.experience)
			)
				.apply(instance, (price, structure, nameKey, decoration, maxUses, experience) -> new JsonSellMapOption(price, structure, nameKey, decoration, maxUses, experience)));
	
	public static final Identifier ID = Identifier.of("sell_map");
	private final int price;
	private final TagKey<Structure> structure;
	private final String nameKey;
	private final RegistryEntry<MapDecorationType> decoration;
	private final int maxUses;
	private final int experience;
	
	public JsonSellMapOption()
	{
		this(13, (TagKey<Structure>)StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.MONUMENT, 12, 5);
	}
	
	public JsonSellMapOption(int price, TagKey<Structure> structure, String nameKey, RegistryEntry<MapDecorationType> decoration, int maxUses, int experience)
	{
		this.price = price;
		this.structure = structure;
		this.nameKey = nameKey;
		this.decoration = decoration;
		this.maxUses = maxUses;
		this.experience = experience;
	}
	
	public Identifier id() { return ID; }
	
	public TradeOffer create(Entity entity, Random random)
	{
		if(entity.getWorld().isClient())
			return null;
		
		ServerWorld serverWorld = (ServerWorld)entity.getWorld();
		BlockPos blockPos = serverWorld.locateStructure(this.structure, entity.getBlockPos(), 100, true);
		if(blockPos != null)
		{
			ItemStack itemStack = FilledMapItem.createMap(serverWorld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
			FilledMapItem.fillExplorationMap(serverWorld, itemStack);
			MapState.addDecorationsNbt(itemStack, blockPos, "+", this.decoration);
			itemStack.set(DataComponentTypes.ITEM_NAME, Text.translatable(this.nameKey));
			return new TradeOffer(new TradedItem(Items.EMERALD, this.price), Optional.of(new TradedItem(Items.COMPASS)), itemStack, this.maxUses, this.experience, 0.2f);
		}
		return null;
	}
	
	public JsonElement writeToJson() { return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow(); }
	
	public TradeFactory fromJson(JsonElement element) { return CODEC.parse(JsonOps.INSTANCE, element.getAsJsonObject()).getOrThrow(); }
}
