package com.lying.neoforge.data;

import java.util.Set;

import com.lying.data.HOTemplatePoolProvider;
import com.lying.reference.Reference;

import net.minecraft.data.DataOutput;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class HODataGeneratorsForge
{
	public static void onGatherDataEvent(GatherDataEvent event)
	{
		DataOutput output = event.getGenerator().getPackOutput();
		
		DatapackBuiltinEntriesProvider provider = new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), new RegistryBuilder()
				.addRegistry(RegistryKeys.TEMPLATE_POOL, HOTemplatePoolProvider::new),
				Set.of(Reference.ModInfo.MOD_ID));
		
		event.getGenerator().addProvider(true, provider);
	}

}
