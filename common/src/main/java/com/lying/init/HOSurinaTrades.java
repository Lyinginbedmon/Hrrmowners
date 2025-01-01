package com.lying.init;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lying.Hrrmowners;
import com.lying.data.ReloadListener;
import com.lying.data.trading.JsonBuyOption;
import com.lying.data.trading.JsonEmptyOption;
import com.lying.data.trading.JsonEnchantBookOption;
import com.lying.data.trading.JsonSellEnchantedToolOption;
import com.lying.data.trading.JsonSellMapOption;
import com.lying.data.trading.JsonSellOption;
import com.lying.data.trading.JsonSellSuspiciousStewOption;
import com.lying.reference.Reference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.architectury.registry.ReloadListenerRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.village.TradeOffers.Factory;
import net.minecraft.village.VillagerProfession;

public class HOSurinaTrades implements ReloadListener<Map<Identifier, JsonObject>>
{
	public final Map<VillagerProfession, Int2ObjectMap<Factory[]>> TRADE_MAP = new HashMap<>();
	
	private static final Map<Identifier, Supplier<TradeFactory>> TRADE_FACTORIES = new HashMap<>();
	public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	public static final String FILE_PATH = "trades";
	
	private static HOSurinaTrades INSTANCE;
	
	public static void init()
	{
		INSTANCE = new HOSurinaTrades();
		ReloadListenerRegistry.register(ResourceType.SERVER_DATA, INSTANCE, INSTANCE.getId());
	}
	
	public static HOSurinaTrades instance() { return INSTANCE; }
	
	public static void registerTradeFactory(Identifier id, Supplier<TradeFactory> supplierIn)
	{
		TRADE_FACTORIES.put(id, supplierIn);
	}
	
	@NotNull
	public static TradeFactory getFactory(Identifier id)
	{
		return TRADE_FACTORIES.containsKey(id) ? TRADE_FACTORIES.get(id).get() : getFactory(JsonEmptyOption.ID);
	}
	
	static
	{
		registerTradeFactory(JsonEmptyOption.ID, JsonEmptyOption::new);
		registerTradeFactory(JsonBuyOption.ID, JsonBuyOption::new);
		registerTradeFactory(JsonSellOption.ID, JsonSellOption::new);
		registerTradeFactory(JsonSellEnchantedToolOption.ID, JsonSellEnchantedToolOption::new);
		registerTradeFactory(JsonEnchantBookOption.ID, JsonEnchantBookOption::new);
		registerTradeFactory(JsonSellSuspiciousStewOption.ID, JsonSellSuspiciousStewOption::new);
		registerTradeFactory(JsonSellMapOption.ID, JsonSellMapOption::new);
	}
	
	/** Returns a map of trade options per level configured in the datapack for Surina */
	@NotNull
	public Int2ObjectMap<Factory[]> getTradeOptions(VillagerProfession profession)
	{
		return TRADE_MAP.getOrDefault(profession, new TradeOptions());
	}
	
	/** Map of profession level to array of trade entries */
	public static class TradeOptions extends Int2ObjectOpenHashMap<Factory[]>
	{
		private static final long serialVersionUID = 1L;
		
		public TradeOptions() { }
		
		public TradeOptions(Map<Integer, TradeFactory[]> valuesIn)
		{
			putAll(valuesIn);
		}
		
		public static TradeOptions fromJsonList(JsonElement list)
		{
			TradeOptions options = new TradeOptions();
			if(list.isJsonArray())
				list.getAsJsonArray().forEach(element -> TradesEntry.CODEC.parse(JsonOps.INSTANCE, element.getAsJsonObject()).ifSuccess(entry -> options.put(entry.level, entry.trades.toArray(new Factory[0]))));
			return options;
		}
		
		@SuppressWarnings("deprecation")
		public JsonElement toJsonList()
		{
			JsonObject obj = new JsonObject();
			JsonArray list = new JsonArray();
			entrySet().forEach(entry -> list.add((new TradesEntry(entry.getKey(), entry.getValue()).toJson())));
			obj.add("Trades", list);
			return obj;
		}
		
		private static class TradesEntry
		{
			public static final Codec<TradesEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.INT.fieldOf("level").forGetter(o -> o.level),
					TradeFactory.CODEC.listOf().fieldOf("trades").forGetter(o -> o.trades)
					)
						.apply(instance, (a,b) -> new TradesEntry(a, b.toArray(new Factory[0]))));
			
			private final int level;
			private final List<TradeFactory> trades = Lists.newArrayList();
			
			private TradesEntry(int levelIn, Factory[] tradesIn)
			{
				level = levelIn;
				for(Factory fac : tradesIn)
					if(fac instanceof TradeFactory)
						trades.add((TradeFactory)fac);
			}
			
			public JsonElement toJson() { return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow(); }
		}
	}
	
	/** A JSON-storeable version of TradeOffers.Factory */
	public interface TradeFactory extends Factory
	{
		public static final PrimitiveCodec<TradeFactory> CODEC = new PrimitiveCodec<TradeFactory>() 
		{
			public <T> DataResult<TradeFactory> read(DynamicOps<T> arg0, T arg1)
			{
				return arg0 == JsonOps.INSTANCE ? DataResult.success(load((JsonElement)arg1)) : null;
			}
			
			@SuppressWarnings("unchecked")
			public <T> T write(DynamicOps<T> arg0, TradeFactory arg1)
			{
				return arg0 == JsonOps.INSTANCE ? (T)arg1.toJson() : null;
			}
		};
		
		@Nullable
		public static TradeFactory load(JsonElement element)
		{
			if(!element.isJsonObject())
				return null;
			JsonObject obj = element.getAsJsonObject();
			Identifier id = Identifier.of(obj.get("id").getAsString());
			return HOSurinaTrades.getFactory(id).fromJson(element);
		}
		
		public Identifier id();
		
		/** Returns this factory stored in JSON data with its registry ID */
		public default JsonElement toJson()
		{
			JsonObject obj = writeToJson().getAsJsonObject();
			obj.addProperty("id", id().toString());
			return obj;
		}
		
		public JsonElement writeToJson();
		
		public TradeFactory fromJson(JsonElement element);
	}

	@Override
	public CompletableFuture<Map<Identifier, JsonObject>> load(ResourceManager manager, Profiler profiler, Executor executor)
	{
		return CompletableFuture.supplyAsync(() -> 
		{
			Map<Identifier, JsonObject> objects = new HashMap<>();
			manager.findAllResources(FILE_PATH, Predicates.alwaysTrue()).forEach((fileName,fileSet) -> 
			{
				// The datapack source this profession came from
				String namespace = fileName.getNamespace();
				
				// The filename of this profession, to be used as registry name
				String fullPath = fileName.getPath();
				fullPath = fullPath.substring(fullPath.lastIndexOf('/') + 1);
				if(fullPath.endsWith(".json"))
					fullPath = fullPath.substring(0, fullPath.length() - 5);
				Identifier registryID = Identifier.of(namespace, fullPath);
				
				Resource file = fileSet.getFirst();
				try
				{
					objects.put(registryID, JsonHelper.deserialize(GSON, (Reader)file.getReader(), JsonObject.class));
				}
				catch(Exception e) { Hrrmowners.LOGGER.error("Error while loading trades "+fileName.toString()); }
			});
			return objects;
		});
	}
	
	public CompletableFuture<Void> apply(Map<Identifier, JsonObject> data, ResourceManager manager, Profiler profiler, Executor executor)
	{
		return CompletableFuture.runAsync(() -> 
		{
			Hrrmowners.LOGGER.info(" # Loading configured Surina trades...");
			TRADE_MAP.clear();
			for(Entry<Identifier, JsonObject> prep : data.entrySet())
			{
				VillagerProfession profession = Registries.VILLAGER_PROFESSION.get(prep.getKey());
				if(profession == null)
					continue;
				TradeOptions trades = TradeOptions.fromJsonList(prep.getValue().get("Trades").getAsJsonArray());
				TRADE_MAP.put(profession, trades);
				
				int tally = 0;
				for(Factory[] array : trades.values())
					tally += array.length;
				Hrrmowners.LOGGER.info(" # # Loaded {} trades for {}", tally, profession == null ? "NULL ("+prep.getKey().toString()+")" : prep.getKey().toString());
			}
		});
	}
	
	public Identifier getId() { return Reference.ModInfo.prefix("trades"); }
}
