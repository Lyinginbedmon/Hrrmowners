package com.lying.fabric.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Lists;
import com.lying.data.trading.DefaultTradeOffers;
import com.lying.reference.Reference;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput.OutputType;
import net.minecraft.data.DataOutput.PathResolver;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class HOTradesProvider implements DataProvider
{
	private final PathResolver path;
	private final CompletableFuture<WrapperLookup> wrapperLookup;
	
	public HOTradesProvider(FabricDataOutput generator, CompletableFuture<WrapperLookup> managerIn)
	{
		this.path = generator.getResolver(OutputType.DATA_PACK, "trades/");
		this.wrapperLookup = managerIn;
	}
	
	public CompletableFuture<?> run(DataWriter dataWriter)
	{
		return wrapperLookup.thenCompose(lookup -> 
		{
			List<CompletableFuture<?>> futures = Lists.newArrayList();
			DefaultTradeOffers.TRADES.entrySet().forEach(entry -> 
						futures.add(DataProvider.writeToPath(dataWriter, entry.getValue().toJsonList(), this.path.resolveJson(Reference.ModInfo.prefix(entry.getKey().id())))));
			return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		});
	}
	
	public String getName() { return Reference.ModInfo.MOD_NAME + " default trades"; }
}
